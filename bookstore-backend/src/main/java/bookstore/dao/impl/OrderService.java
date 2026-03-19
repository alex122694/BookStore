package bookstore.dao.impl;

import java.math.BigDecimal;

import java.util.List;

import org.hibernate.Session;

import bookstore.bean.OrderItem;
import bookstore.bean.Orders;
import bookstore.dao.OrderItemDao;
import bookstore.dao.OrdersDao;

public class OrderService {

	private OrdersDao ordersDao;
	private OrderItemDao orderItemDao;

	public OrderService(Session session) {
		this.ordersDao = new OrdersDao(session);
		this.orderItemDao = new OrderItemDao(session);
	}

	// 建立新訂單 (包含訂單主檔與明細)
	public void createOrder(Orders order, List<OrderItem> items) {
		// 計算訂單總額
		BigDecimal totalAmount = new BigDecimal(0);
		for (OrderItem item : items) {
			// 通過Controller傳入的訂單明細Subtotal，計算訂單總價
			totalAmount = totalAmount.add(item.getSubtotal());
		}
		// 將計算後的總金額設定給 Orders 物件
		order.setTotalAmount(totalAmount);

		// 1. 新增訂單主檔 (會自動回填 OrderId)
		ordersDao.insertOrder(order);

		Integer orderId = order.getOrderId();
		if (orderId == null) {
			throw new RuntimeException("訂單建立失敗，無法取得 Order ID");
		}

		// 2. 新增訂單明細
		for (OrderItem item : items) {
			item.setOrders(order); // 關聯到剛剛建立的訂單
			orderItemDao.insertOrderItem(item);
		}
	}

	// 在既有訂單中新增一筆或多筆訂單明細，並自動重新計算訂單總金額
	public void addItemsToOrder(Integer orderId, List<OrderItem> newItems) {
		if (newItems == null || newItems.isEmpty()) {
			return;
		}

		// 1. 檢查訂單是否存在
		Orders order = ordersDao.findOrderById(orderId);
		if (order == null) {
			throw new RuntimeException("找不到訂單 ID: " + orderId + "，無法新增明細。");
		}

		// 2. 新增新的訂單明細
		for (OrderItem item : newItems) {
			item.getOrders().setOrderId(orderId); // 當前訂單id
			orderItemDao.insertOrderItem(item);
		}

		// 3. 重新計算訂單金額
		// 注意: 這邊應該要查出"所有"明細來重算，因為 Hibernate Session 快取的關係，
		// 剛剛 insert 的 item 可能還沒 flush 到資料庫，但用 findOrderItemsByOrderId 查出的 list 是否包含新
		// items?
		// 如果是用 HQL 查詢，通常會 flush session。
		// 為了確保正確，我們可以直接用 order.getItems() 如果有設定關聯，
		// 但這邊 OrderItem 是維護關聯的一方。我們還是查 DAO。
		List<OrderItem> allItems = orderItemDao.findOrderItemsByOrderId(orderId);

		BigDecimal newTotal = new BigDecimal(0);
		for (OrderItem item : allItems) {
			newTotal = newTotal.add(item.getSubtotal());
		}

		// 4. 更新訂單主檔總金額
		order.setTotalAmount(newTotal);
		ordersDao.updateOrder(order);
	}

	// 更新訂單
	public void updateOrder(Orders order) {
		ordersDao.updateOrder(order);
	}

	// 更新單一訂單明細，並自動重新計算訂單總金額
	public void updateOrderItem(OrderItem item) {
		// 1. 更新該筆明細
		orderItemDao.updateOrderItem(item);

		// 2. 重新計算總金額
		List<OrderItem> items = orderItemDao.findOrderItemsByOrderId(item.getOrders().getOrderId());

		BigDecimal newTotal = new BigDecimal(0);
		for (OrderItem i : items) {
			newTotal = newTotal.add(i.getSubtotal());
		}

		// 3. 更新訂單主檔總金額
		Orders order = ordersDao.findOrderById(item.getOrders().getOrderId());
		order.setTotalAmount(newTotal);
		ordersDao.updateOrder(order);
	}

	// 刪除整筆訂單 (包含該訂單的所有明細)(硬刪除，大概不會用，建著以備不時之需)
	public void deleteOrder(Integer orderId) {
		// 先刪除所有訂單明細(防止FK問題)
		// 雖然 Hibernate 有 cascade，但這邊手動刪除確保邏輯一致
		orderItemDao.deleteOrderItemsByOrderId(orderId);

		// 再刪除訂單主檔
		ordersDao.deleteOrderById(orderId);
	}
	
	// 取消訂單(軟刪除)(將訂單從ActiveOrders轉變成取消中的訂單)
		public void processCancelOrder(Integer orderId) {
		    Orders order = ordersDao.findOrderById(orderId);
		    if (order != null) {
		       // 更新訂單狀態
		    	order.setOrderStatus("已取消");
		        ordersDao.updateOrder(order);
		    } else {
		        throw new RuntimeException("找不到編號為 " + orderId + " 的訂單，無法取消。");
		    }
		}
		
	// 還原訂單 (從取消清單轉回活動訂單)
		public void processRestoreOrder(Integer orderId) {
		    Orders order = ordersDao.findOrderById(orderId);
		    
		    if (order != null) {
		        // 安全檢查：只有「已取消」的訂單才能被還原 (防止誤動已退款訂單)
		        if ("已取消".equals(order.getOrderStatus())) {
		            // 更新狀態回活動訂單的起始狀態，例如 "待處理"
		            order.setOrderStatus("待處理");
		            ordersDao.updateOrder(order);
		        } else {
		            throw new RuntimeException("訂單編號 " + orderId + " 狀態非「已取消」，無法還原。");
		        }
		    } else {
		        throw new RuntimeException("找不到編號為 " + orderId + " 的訂單，無法還原。");
		    }
		}

	// 刪除單一訂單明細，並自動重新計算訂單總金額
	public void deleteOrderItem(Integer orderItemId) {
		// 1. 先查出這筆明細屬於哪張訂單 (為了後續重算總額)
		OrderItem itemToDelete = orderItemDao.findOrderItemById(orderItemId);
		if (itemToDelete == null) {
			throw new RuntimeException("找不到該筆訂單明細: " + orderItemId);
		}
		Integer orderId = itemToDelete.getOrders().getOrderId();

		// 2. 刪除該筆明細
		orderItemDao.deleteOrderItemById(orderItemId);

		// 3. 重新計算總金額
		List<OrderItem> items = orderItemDao.findOrderItemsByOrderId(orderId);
		// 注意: items 列表可能還包含剛剛刪除的物件(如果還沒 flush)，需要過濾掉
		// 但我們是依靠 HQL 查詢 `findOrderItemsByOrderId`，這會觸發 flush，所以理論上不會查到被刪除的。

		BigDecimal newTotal = new BigDecimal(0);
		for (OrderItem item : items) {
			// Double check: ID 不等於被刪除的 ID
			if (!item.getOrderItemId().equals(orderItemId)) {
				newTotal = newTotal.add(item.getSubtotal());
			}
		}
		// *Correction*: Typo in previous line `getOrderItemItemId` is likely wrong.
		// OrderItem.java has `getOrderItemId()`.
		// Also simpler: HQL query won't return deleted item.

		// Let's re-calculate cleanly.
		newTotal = BigDecimal.ZERO;
		for (OrderItem item : items) {
			newTotal = newTotal.add(item.getSubtotal());
		}

		// 4. 更新訂單主檔總金額
		Orders order = ordersDao.findOrderById(orderId);
		order.setTotalAmount(newTotal);
		ordersDao.updateOrder(order);
	}

	// 查詢所有訂單
	public List<Orders> getAllOrders() {
		return ordersDao.findAllOrders();
	}

	// 查詢未被取消與退款的訂單
	public List<Orders> getAllActiveOrders() {
	    return ordersDao.findActiveOrders();
	}

	// 查詢已被取消與退款的訂單
	public List<Orders> getAllCancelOrders() {
	    return ordersDao.findCancelAndRefundedOrders();
	}
	
	// 查詢使用者的訂單
	public List<Orders> getOrdersByUserId(Integer userId) {
		return ordersDao.findOrdersByUserId(userId);
	}

	// 查詢單筆訂單資訊
	public Orders getOrderById(Integer orderId) {
		return ordersDao.findOrderById(orderId);
	}

	// 查詢某張訂單的所有明細
	public List<OrderItem> getOrderItems(Integer orderId) {
		return orderItemDao.findOrderItemsByOrderId(orderId);
	}

	// 查詢所有訂單明細
	public List<OrderItem> getAllOrderItems() {
		return orderItemDao.findAllOrderItems();
	}
}
