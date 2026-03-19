package bookstore.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bookstore.bean.OrderItem;
import bookstore.bean.OrderReturnBean;
import bookstore.bean.Orders;
import bookstore.bean.UserBean;
import bookstore.aop.BusinessException;
import bookstore.bean.BooksBean;
import bookstore.bean.Cart;
import bookstore.bean.CouponBean;
import bookstore.repository.BookRepository;
import bookstore.repository.CartRepository;
import bookstore.repository.OrderItemRepository;
import bookstore.repository.OrderReturnRepository;
import bookstore.repository.OrdersRepository;
import bookstore.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import bookstore.repository.CouponRepository;
import bookstore.repository.UserCouponRepository; // Added
import bookstore.bean.UserCouponBean; // Added
import bookstore.dto.BookSalesDTO;
import bookstore.dto.CheckoutRequest;
import bookstore.dto.OrderFullUpdateDTO;
import bookstore.dto.ItemUpdateDTO;

@Service
@Transactional
public class OrderService {

	@Autowired
	private OrdersRepository ordersRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserCouponRepository userCouponRepository;

	@Autowired
	private OrderReturnRepository orderReturnRepository;

	// 從購物車轉訂單 (Checkout Transcation)
	@SuppressWarnings("null")
	public Orders createOrderFromCart(Integer userId, CheckoutRequest request) {

		// 1. 取得使用者資料
		UserBean user = userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(400, "找不到使用者"));

		// 2. 取得使用者購物車
		List<Cart> cartItems = cartRepository.findByUserId(userId);
		if (cartItems.isEmpty()) {
			throw new BusinessException(400, "購物車是空的");
		}

		// 3. 檢查庫存 & 計算總金額
		BigDecimal totalAmount = BigDecimal.ZERO;

		for (Cart cart : cartItems) {
			BooksBean book = cart.getBooksBean();

			// 檢查上架狀態
			if (book.getOnShelf() != 1) {
				throw new BusinessException(400, "書籍 " + book.getBookName() + " 已下架，請移除後再結帳");
			}

			// 檢查庫存
			if (book.getStock() < cart.getQuantity()) {
				throw new BusinessException(400,
						"書籍 " + book.getBookName() + " 庫存不足，僅剩 " + book.getStock() + " 本");
			}

			// 扣除庫存
			book.setStock(book.getStock() - cart.getQuantity());
			bookRepository.save(book);

			// 計算書籍小計
			BigDecimal subtotal = book.getPrice().multiply(new BigDecimal(cart.getQuantity()));
			totalAmount = totalAmount.add(subtotal);
		}

		// 4. 計算運費 (宅配<1000 => 50, 超商<350 => 50)與判斷付款狀態
		BigDecimal shippingFee = BigDecimal.ZERO;
		String delivery = request.getDeliveryMethod(); // 宅配到府 or 超商取貨
		String paymentStatus = "";
		String paymentMethod = request.getPaymentMethod();

		if ("宅配到府".equals(delivery)) {
			if (totalAmount.compareTo(new BigDecimal(1000)) < 0) {
				shippingFee = new BigDecimal(50);
			} else {
				shippingFee = new BigDecimal(0);
			}
		} else if ("超商取貨".equals(delivery)) {
			if (totalAmount.compareTo(new BigDecimal(350)) < 0) {
				shippingFee = new BigDecimal(50);
			} else {
				shippingFee = new BigDecimal(0);
			}
		}

		String orderStatus = "待出貨"; // 預設

		if ("信用卡".equals(paymentMethod)) {
			paymentStatus = "未付款";
			orderStatus = "待付款";
		} else if ("貨到付款".equals(paymentMethod)) {
			paymentStatus = "未付款";
			orderStatus = "待出貨";
		}

		// 計算實付金額
		BigDecimal finalAmount = totalAmount.add(shippingFee);

		// 5. 建立訂單
		Orders order = new Orders();
		order.setUserBean(user);
		order.setTotalAmount(totalAmount); // 商品總額
		order.setShippingFee(shippingFee); // 運費
		order.setFinalAmount(finalAmount); // 實付總額

		order.setPaymentMethod(request.getPaymentMethod());
		order.setPaymentStatus(paymentStatus);
		order.setOrderStatus(orderStatus);

		order.setRecipientAt(request.getRecipientName());
		order.setPhone(request.getRecipientPhone());
		order.setAddress(request.getAddress()); // 地址或超商門市名稱
		order.setDeliveryMethod(delivery);
		order.setCouponId(request.getCouponId());

		order.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
		order.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

		ordersRepository.save(order);

		// 6. 建立訂單明細
		for (Cart cart : cartItems) {
			OrderItem item = new OrderItem();
			item.setOrders(order);
			item.setBooksBean(cart.getBooksBean());
			item.setQuantity(cart.getQuantity());
			item.setPrice(cart.getBooksBean().getPrice());
			item.setSubtotal(cart.getBooksBean().getPrice().multiply(new BigDecimal(cart.getQuantity())));

			orderItemRepository.save(item);
		}

		// 8. 處理優惠券 (如果有)
		if (order.getCouponId() != null) {
			// 使用 UserCouponRepository 查找 UserCouponBean (代表使用者領取的優惠券)
			UserCouponBean userCoupon = userCouponRepository.findById(order.getCouponId())
					.orElseThrow(() -> new BusinessException(400, "優惠券不存在"));

			// 驗證優惠券歸屬
			if (!userCoupon.getUserId().equals(order.getUserBean().getUserId())) {
				throw new BusinessException(400, "這張優惠券不屬於您");
			}

			// 驗證是否已使用
			if (userCoupon.getStatus() == 1) {
				throw new BusinessException(400, "優惠券已使用");
			}

			// 獲取優惠券定義 (CouponBean) 以檢查規則
			CouponBean couponDef = userCoupon.getCouponBean();

			// 驗證低消
			if (order.getTotalAmount().compareTo(couponDef.getMinSpend()) < 0) {
				throw new BusinessException(400, "未達優惠券最低消費金額: " + couponDef.getMinSpend());
			}

			// 套用折扣
			BigDecimal discount = couponDef.getDiscountAmount();
			order.setDiscount(discount);

			// 重新計算實付金額
			// 實付金額 = 總金額 + 運費 - 折扣金額
			BigDecimal newFinal = order.getFinalAmount().subtract(discount);
			if (newFinal.compareTo(BigDecimal.ZERO) < 0) {
				newFinal = BigDecimal.ZERO;
			}
			order.setFinalAmount(newFinal);

			// 標記已使用
			userCoupon.setStatus(1);
			userCoupon.setUsedAt(new java.sql.Timestamp(System.currentTimeMillis()));
			userCouponRepository.save(userCoupon);
			ordersRepository.save(order); // 更新訂單的折扣和最終金額
		}

		// 7. 清空購物車
		cartRepository.deleteByUserId(userId);
		return order;
	}

	// 後台新增訂單
	public void insertOrder(Orders order, List<OrderItem> items) {
		// 0. 檢查庫存與上架狀態 (新增邏輯)
		for (OrderItem item : items) {
			Integer bookId = item.getBooksBean().getBookId();
			// 必須重新查詢以確保獲取最新庫存與狀態
			BooksBean book = bookRepository.findById(bookId)
					.orElseThrow(() -> new BusinessException(400, "找不到書籍 ID: " + bookId));

			// 檢查上架狀態
			if (book.getOnShelf() != 1) {
				throw new BusinessException(400, "書籍 " + book.getBookName() + " 已下架，無法建立訂單");
			}

			// 檢查庫存
			if (book.getStock() < item.getQuantity()) {
				throw new BusinessException(400,
						"書籍 " + book.getBookName() + " 庫存不足，僅剩 " + book.getStock() + " 本");
			}

			// 更新 Item 的 BookBean 確保數據一致
			item.setBooksBean(book);
		}

		// 計算總金額
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (OrderItem item : items) {
			BigDecimal subtotal = item.getSubtotal();
			totalAmount = totalAmount.add(subtotal);
		}
		// 總金額賦值
		order.setTotalAmount(totalAmount);

		// 計算運費 (宅配<1000 => 50, 超商<350 => 50)
		BigDecimal shippingFee = BigDecimal.ZERO;
		String delivery = order.getDeliveryMethod();

		if ("HOME".equals(delivery) || "宅配到府".equals(delivery)) {
			if (totalAmount.compareTo(new BigDecimal(1000)) < 0) {
				shippingFee = new BigDecimal(50);
			} else {
				shippingFee = new BigDecimal(0);
			}
		} else if ("STORE".equals(delivery) || "超商取貨".equals(delivery)) {
			if (totalAmount.compareTo(new BigDecimal(350)) < 0) {
				shippingFee = new BigDecimal(50);
			} else {
				shippingFee = new BigDecimal(0);
			}
		}

		order.setShippingFee(shippingFee);
		order.setFinalAmount(totalAmount.add(shippingFee));

		// 新增訂單
		ordersRepository.save(order);

		// 新增訂單明細並扣除庫存
		for (OrderItem item : items) {
			item.setOrders(order);
			orderItemRepository.save(item);

			// 扣除庫存 (新增邏輯)
			BooksBean book = item.getBooksBean();
			book.setStock(book.getStock() - item.getQuantity());
			bookRepository.save(book);
		}

		// 8. 處理優惠券 (如果有)
		if (order.getCouponId() != null) {
			// 使用 UserCouponRepository 查找 UserCouponBean
			UserCouponBean userCoupon = userCouponRepository.findById(order.getCouponId())
					.orElseThrow(() -> new BusinessException(400, "優惠券不存在"));

			// 驗證優惠券歸屬
			if (!userCoupon.getUserId().equals(order.getUserBean().getUserId())) {
				throw new BusinessException(400, "這張優惠券不屬於您");
			}

			// 驗證是否已使用
			if (userCoupon.getStatus() == 1) {
				throw new BusinessException(400, "優惠券已使用");
			}

			CouponBean couponDef = userCoupon.getCouponBean();

			// 驗證低消
			if (order.getTotalAmount().compareTo(couponDef.getMinSpend()) < 0) {
				throw new BusinessException(400, "未達優惠券最低消費金額: " + couponDef.getMinSpend());
			}

			// 套用折扣
			BigDecimal discount = couponDef.getDiscountAmount();
			order.setDiscount(discount);

			// 重新計算實付金額
			// 實付金額 = 總金額 + 運費 - 折扣金額
			BigDecimal newFinal = order.getFinalAmount().subtract(discount);
			if (newFinal.compareTo(BigDecimal.ZERO) < 0) {
				newFinal = BigDecimal.ZERO;
			}
			order.setFinalAmount(newFinal);

			// 標記已使用
			userCoupon.setStatus(1);
			userCoupon.setUsedAt(new java.sql.Timestamp(System.currentTimeMillis()));
			userCouponRepository.save(userCoupon);
			ordersRepository.save(order); // 更新訂單的折扣和最終金額
		}
	}

	// 針對已有訂單新增明細
	public void addItemsToOrder(Integer orderId, List<OrderItem> newItems) {
		if (newItems == null || newItems.isEmpty()) {
			System.out.println("沒有明細需要新增");
			return;
		}

		Optional<Orders> optional = ordersRepository.findById(orderId);
		Orders order;

		if (optional.isPresent()) {
			order = optional.get();
		} else {
			throw new RuntimeException("找不到訂單 ID: " + orderId);
		}

		for (OrderItem item : newItems) {
			item.setOrders(order);
			orderItemRepository.save(item);
		}
		updateOrderTotalAmount(orderId);// private方法，詳class最下面
	}

	// 更新訂單
	public void updateOrder(Orders order) {
		ordersRepository.save(order);
	}

	// 更新訂單明細
	public void updateOrderItem(OrderItem item) {
		orderItemRepository.save(item);
		updateOrderTotalAmount(item.getOrders().getOrderId());
	}

	// 全局更新訂單 (含狀態與明細)
	public void updateFullOrder(OrderFullUpdateDTO dto) {
		Orders order = ordersRepository.findById(dto.getOrderId())
				.orElseThrow(() -> new BusinessException(400, "找不到訂單 ID: " + dto.getOrderId()));

		// 1. 更新基本資料
		order.setRecipientAt(dto.getRecipientAt());
		order.setPhone(dto.getPhone());
		order.setAddress(dto.getAddress());
		order.setPaymentMethod(dto.getPaymentMethod());
		order.setDeliveryMethod(dto.getDeliveryMethod());

		// 2. 更新狀態與時間點
		String newPaymentStatus = dto.getPaymentStatus();
		if (newPaymentStatus != null && !newPaymentStatus.equals(order.getPaymentStatus())) {
			order.setPaymentStatus(newPaymentStatus);
			if ("已付款".equals(newPaymentStatus)) {
				order.setPaidAt(new Timestamp(System.currentTimeMillis()));
			}
		}

		String newOrderStatus = dto.getOrderStatus();
		if (newOrderStatus != null && !newOrderStatus.equals(order.getOrderStatus())) {
			order.setOrderStatus(newOrderStatus);
			Timestamp now = new Timestamp(System.currentTimeMillis());
			if ("已出貨".equals(newOrderStatus)) {
				order.setShippedAt(now);
			} else if ("已送達".equals(newOrderStatus)) {
				order.setDeliveredAt(now);
			} else if ("已收貨".equals(newOrderStatus)) {
				order.setReceivedAt(now);
			} else if ("已完成".equals(newOrderStatus)) {
				order.setCompletedAt(now);
			}
		}

		// 3. 更新明細
		if (dto.getItems() != null) {
			for (ItemUpdateDTO itemDto : dto.getItems()) {
				OrderItem item = orderItemRepository.findById(itemDto.getOrderItemId())
						.orElseThrow(() -> new BusinessException(400, "找不到明細 ID: " + itemDto.getOrderItemId()));

				// 驗證是否屬於該訂單
				if (!item.getOrders().getOrderId().equals(order.getOrderId())) {
					continue;
				}

				item.setQuantity(itemDto.getQuantity());
				// 重算小計
				item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
				orderItemRepository.save(item);
			}
		}

		// 設定更新時間
		order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

		// 先存基本資料，因為 updateOrderTotalAmount 會重新 fetch
		ordersRepository.save(order);

		// 4. 重算整筆訂單金額 (含運費規則) 並存檔
		updateOrderTotalAmount(order.getOrderId());
	}

	// 刪除訂單(硬刪除)
	// 專題後續採軟刪除，此方法不會用，但保留
	public void deleteOrder(Integer orderId) {
		// 先刪明細再刪訂單
		orderItemRepository.deleteByOrderId(orderId);
		ordersRepository.deleteById(orderId);
	}

	// 取消訂單(軟刪除)
	public void processCancelOrder(Integer orderId) {

		Optional<Orders> optional = ordersRepository.findById(orderId);

		if (optional.isPresent()) {
			Orders order = optional.get();

			// 避免重複取消
			if ("已取消".equals(order.getOrderStatus())) {
				return;
			}

			// 修改訂單狀態為已取消
			order.setOrderStatus("已取消");

			// 庫存回補
			List<OrderItem> items = orderItemRepository.findByOrders_OrderId(orderId);
			for (OrderItem item : items) {
				BooksBean book = item.getBooksBean();
				// 加回庫存
				book.setStock(book.getStock() + item.getQuantity());
				bookRepository.save(book);
			}

			// 更新訂單
			ordersRepository.save(order);
		} else {
			// 沒找到訂單就丟例外
			throw new RuntimeException("找不到訂單 ID: " + orderId);
		}
	}

	// 申請退貨
	public void processReturnOrder(Integer orderId, String reason, String description) {
		Optional<Orders> optional = ordersRepository.findById(orderId);

		if (optional.isPresent()) {
			Orders order = optional.get();

			// 避免重複處理
			if ("已取消".equals(order.getOrderStatus())) {
				// 如果已經取消，檢查是否已經有退貨紀錄，若無則補上
				Optional<OrderReturnBean> existingReturn = orderReturnRepository.findByOrders_OrderId(orderId);
				if (existingReturn.isPresent()) {
					return; // 已經有紀錄且已取消，不處理
				}
				// 若沒紀錄但已取消，可能只是之前單純取消，這裡我們可以選擇不動作或補紀錄
				// 依照需求，這裡視為新申請
			}

			// 1. 建立退貨紀錄
			OrderReturnBean returnBean = new OrderReturnBean();
			returnBean.setOrders(order);
			returnBean.setReason(reason);
			returnBean.setDescription(description);
			orderReturnRepository.save(returnBean);
			// 2. 只有當訂單尚未取消時，才進行狀態更新與庫存回補
			// 如果訂單已經是「已取消」，則不再重複回補庫存
			if (!"已取消".equals(order.getOrderStatus())) {
				order.setOrderStatus("已取消"); // 統一設為已取消

				// 3. 庫存回補
				List<OrderItem> items = orderItemRepository.findByOrders_OrderId(orderId);
				for (OrderItem item : items) {
					BooksBean book = item.getBooksBean();
					book.setStock(book.getStock() + item.getQuantity());
					bookRepository.save(book);
				}

				// 更新修改時間
				order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

				ordersRepository.save(order);
			}

		} else {
			throw new RuntimeException("找不到訂單 ID: " + orderId);
		}
	}

	// 取得退貨資訊
	public OrderReturnBean getReturnRequestByOrderId(Integer orderId) {
		return orderReturnRepository.findByOrders_OrderId(orderId).orElse(null);
	}

	// 刪除訂單明細
	public void deleteOrderItem(Integer orderItemId) {

		Optional<OrderItem> optional = orderItemRepository.findById(orderItemId);

		if (optional.isPresent()) {
			OrderItem item = optional.get();

			// 取得訂單id，方便等等刪除明細後更新訂單總金額
			Integer orderId = item.getOrders().getOrderId();

			// 刪除訂單明細
			orderItemRepository.deleteById(orderItemId);

			// 依據訂單id重算整筆訂單總金額並更新，private方法，詳下面
			updateOrderTotalAmount(orderId);

		} else {
			// 如果找不到該筆明細，丟例外，找不到訂單明細
			throw new RuntimeException("找不到該筆訂單明細");
		}
	}

	// 依據訂單id重算整筆訂單總金額並更新訂單的方法
	private void updateOrderTotalAmount(Integer orderId) {
		List<OrderItem> allItems = orderItemRepository.findByOrders_OrderId(orderId);

		// 重新計算訂單總金額
		BigDecimal newTotal = BigDecimal.ZERO; // 從 0 開始
		for (OrderItem item : allItems) {
			BigDecimal subtotal = item.getSubtotal();
			newTotal = newTotal.add(subtotal);
		}

		// 找要更新的訂單
		Optional<Orders> optional = ordersRepository.findById(orderId);

		if (optional.isPresent()) {
			Orders order = optional.get();
			order.setTotalAmount(newTotal);

			// 重新計算運費
			BigDecimal shippingFee = BigDecimal.ZERO;
			String delivery = order.getDeliveryMethod();

			if ("HOME".equals(delivery) || "宅配到府".equals(delivery)) {
				// 宅配 < 1000 => 50
				if (newTotal.compareTo(new BigDecimal(1000)) < 0) {
					shippingFee = new BigDecimal(50);
				}
			} else if ("STORE".equals(delivery) || "超商取貨".equals(delivery)) {
				// 超商 < 350 => 50 (修正後的門檻)
				if (newTotal.compareTo(new BigDecimal(350)) < 0) {
					shippingFee = new BigDecimal(50);
				}
			}

			order.setShippingFee(shippingFee);

			// 重新計算實付金額
			BigDecimal finalAmount = newTotal.add(shippingFee);
			order.setFinalAmount(finalAmount);

			// 將新的總金額更新進資料庫
			ordersRepository.save(order);
		} else {
			// 找不到訂單，就丟例外
			throw new RuntimeException("找不到訂單，無法更新訂單總額");
		}
	}

	// ---查詢操作，加上readOnly設定可以優化效能---//

	// 查詢全部訂單
	@Transactional(readOnly = true)
	public List<Orders> getAllOrders() {
		return ordersRepository.findAll();
	}

	// 查詢單筆訂單
	@Transactional(readOnly = true)
	public Orders getOrderById(Integer orderId) {
		Optional<Orders> optional = ordersRepository.findById(orderId);
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	// 查詢活動訂單
	@Transactional(readOnly = true)
	public List<Orders> getAllActiveOrders() {
		return ordersRepository.findActiveOrders();
	}

	// 查詢已取消與已退款的訂單
	@Transactional(readOnly = true)
	public List<Orders> getCancelledOrders() {
		return ordersRepository.findCancelAndRefundedOrders();
	}

	// 查詢單一使用者所有訂單
	@Transactional(readOnly = true)
	public List<Orders> getOrdersByUserId(Integer userId) {
		return ordersRepository.findByUserBean_UserId(userId);
	}

	// 查詢單筆訂單所有明細
	@Transactional(readOnly = true)
	public List<OrderItem> getOrderItemsByOrderId(Integer orderId) {
		return orderItemRepository.findByOrders_OrderId(orderId);
	}

	// 查詢所有訂單明細
	@Transactional(readOnly = true)
	public List<OrderItem> getAllOrderItems() {
		return orderItemRepository.findAll();
	}

	@Transactional
	public void updatePaymentStatus(Integer orderId, String paymentStatus) {
		Orders order = ordersRepository.findById(orderId)
				.orElseThrow(() -> new BusinessException(400, "找不到訂單 ID: " + orderId));

		order.setPaymentStatus(paymentStatus);

		if ("已付款".equals(paymentStatus)) {
			order.setOrderStatus("待出貨"); // 假設付款成功後轉為待出貨
			order.setPaidAt(new Timestamp(System.currentTimeMillis()));
			order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		}

		ordersRepository.save(order);
	}

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional(readOnly = true)
	public List<BookSalesDTO> getTopSellingBooks() {
		return getTopSellingBooks(null, null);
	}

	@Transactional(readOnly = true)
	public List<BookSalesDTO> getTopSellingBooks(Timestamp start, Timestamp end) {
		StringBuilder jpql = new StringBuilder(
				"SELECT new bookstore.dto.BookSalesDTO(b.bookName, SUM(oi.quantity)) " +
						"FROM OrderItem oi " +
						"JOIN oi.booksBean b " +
						"JOIN oi.orders o " +
						"WHERE o.orderStatus NOT IN ('已取消', '已退款') ");

		if (start != null && end != null) {
			jpql.append("AND o.createdAt BETWEEN :start AND :end ");
		}

		jpql.append("GROUP BY b.bookName ORDER BY SUM(oi.quantity) DESC");

		var query = entityManager.createQuery(jpql.toString(), BookSalesDTO.class);

		if (start != null && end != null) {
			query.setParameter("start", start);
			query.setParameter("end", end);
		}

		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public List<BookSalesDTO> getTopSellingBooksFull(Timestamp start, Timestamp end) {
	    StringBuilder jpql = new StringBuilder(
	            "SELECT new bookstore.dto.BookSalesDTO(" +
	                    "b.bookId, b.bookName, b.author, b.price, bi.imageUrl, b.shortDesc, SUM(oi.quantity)) " + // 💡 加入 b.shortDesc
	                    "FROM OrderItem oi " +
	                    "JOIN oi.booksBean b " +
	                    "LEFT JOIN b.bookImageBean bi " +
	                    "JOIN oi.orders o " +
	                    "WHERE o.orderStatus NOT IN ('已取消', '已退款') " +
	                    "AND b.onShelf = 1 ");

	    if (start != null && end != null) {
	        jpql.append("AND o.createdAt BETWEEN :start AND :end ");
	    }

	    jpql.append("GROUP BY b.bookId, b.bookName, b.author, b.price, bi.imageUrl, b.shortDesc " +
	            "ORDER BY SUM(oi.quantity) DESC");

	    var query = entityManager.createQuery(jpql.toString(), BookSalesDTO.class);

	    if (start != null && end != null) {
	        query.setParameter("start", start);
	        query.setParameter("end", end);
	    }

	    List<BookSalesDTO> resultList = query.setMaxResults(4).getResultList();

	    for (BookSalesDTO dto : resultList) {
	        bookRepository.findById(dto.getBookId()).ifPresent(fullBook -> {
	            dto.setGenres(fullBook.getGenres());
	        });
	    }

	    return resultList;
	}

	@Transactional(readOnly = true)
	public BigDecimal getSalesRevenue(Timestamp start, Timestamp end) {
		StringBuilder jpql = new StringBuilder(
				"SELECT SUM(o.finalAmount) FROM Orders o WHERE o.orderStatus NOT IN ('已取消', '已退款') ");

		if (start != null && end != null) {
			jpql.append("AND o.createdAt BETWEEN :start AND :end");
		}

		var query = entityManager.createQuery(jpql.toString(), BigDecimal.class);

		if (start != null && end != null) {
			query.setParameter("start", start);
			query.setParameter("end", end);
		}

		BigDecimal result = query.getSingleResult();
		return result != null ? result : BigDecimal.ZERO;
	}

	@Transactional(readOnly = true)
	public List<bookstore.dto.MonthlySalesDTO> getRecentSalesTrends() {
		// Calculate start date: 1st day of 11 months ago (covering a 12-month span
		// including current month)
		java.time.LocalDate now = java.time.LocalDate.now();
		java.time.LocalDate start = now.minusMonths(11).withDayOfMonth(1);
		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());

		String jpql = "SELECT new bookstore.dto.MonthlySalesDTO(" +
				"function('year', o.createdAt), " +
				"function('month', o.createdAt), " +
				"SUM(o.finalAmount), " +
				"COUNT(o)) " +
				"FROM Orders o " +
				"WHERE o.orderStatus NOT IN ('已取消', '已退款') " +
				"AND o.createdAt >= :startDate " +
				"GROUP BY function('year', o.createdAt), function('month', o.createdAt) " +
				"ORDER BY function('year', o.createdAt), function('month', o.createdAt)";

		return entityManager.createQuery(jpql, bookstore.dto.MonthlySalesDTO.class)
				.setParameter("startDate", startTs)
				.getResultList();
	}

	@Transactional(readOnly = true)
	public bookstore.dto.SalesOverviewDTO getSalesOverview(Timestamp start, Timestamp end) {
		// 1. Total Revenue
		StringBuilder revenueJpql = new StringBuilder(
				"SELECT SUM(o.finalAmount) FROM Orders o WHERE o.orderStatus NOT IN ('已取消', '已退款') ");
		if (start != null && end != null) {
			revenueJpql.append("AND o.createdAt BETWEEN :start AND :end");
		}
		var revenueQuery = entityManager.createQuery(revenueJpql.toString(), BigDecimal.class);
		if (start != null && end != null) {
			revenueQuery.setParameter("start", start);
			revenueQuery.setParameter("end", end);
		}
		BigDecimal totalRevenue = revenueQuery.getSingleResult();
		if (totalRevenue == null)
			totalRevenue = BigDecimal.ZERO;

		// 2. Total Orders
		StringBuilder countJpql = new StringBuilder(
				"SELECT COUNT(o) FROM Orders o WHERE o.orderStatus NOT IN ('已取消', '已退款') ");
		if (start != null && end != null) {
			countJpql.append("AND o.createdAt BETWEEN :start AND :end");
		}
		var countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
		if (start != null && end != null) {
			countQuery.setParameter("start", start);
			countQuery.setParameter("end", end);
		}
		Long totalOrders = countQuery.getSingleResult();
		if (totalOrders == null)
			totalOrders = 0L;

		// 3. Total Books Sold
		StringBuilder booksJpql = new StringBuilder(
				"SELECT SUM(oi.quantity) FROM OrderItem oi JOIN oi.orders o WHERE o.orderStatus NOT IN ('已取消', '已退款') ");
		if (start != null && end != null) {
			booksJpql.append("AND o.createdAt BETWEEN :start AND :end");
		}
		var booksQuery = entityManager.createQuery(booksJpql.toString(), Long.class);
		if (start != null && end != null) {
			booksQuery.setParameter("start", start);
			booksQuery.setParameter("end", end);
		}
		Long totalBooksSold = booksQuery.getSingleResult();
		if (totalBooksSold == null)
			totalBooksSold = 0L;

		return new bookstore.dto.SalesOverviewDTO(totalRevenue, totalOrders, totalBooksSold);
	}

}