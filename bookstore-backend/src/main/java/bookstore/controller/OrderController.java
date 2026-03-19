package bookstore.controller;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import bookstore.bean.BooksBean;
import bookstore.bean.OrderItem;
import bookstore.bean.OrderReturnBean;
import bookstore.bean.Orders;
import bookstore.bean.UserBean;
import bookstore.service.OrderService;
import bookstore.service.bookService;
import bookstore.dto.BookSalesDTO;
import bookstore.dto.OrderFullUpdateDTO;
import bookstore.dto.OrderReturnRequest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private bookService bookService;

	// ==================新增類 Controller==================//

	// 建立新訂單
	@PostMapping("/order/insert")
	public String processInsertOrder(HttpServletRequest request, Model model) {
		try {
			// 接收表單資料與set物件
			Orders order = new Orders();
			Integer userId = Integer.parseInt(request.getParameter("userId"));
			UserBean user = new UserBean();
			user.setUserId(userId);
			order.setUserBean(user);
			order.setRecipientAt(request.getParameter("recipientName"));
			order.setAddress(request.getParameter("address"));
			order.setPhone(request.getParameter("phone"));
			String paymentMethod = request.getParameter("paymentMethod");
			order.setPaymentMethod(paymentMethod);

			// 新增配送方式
			String deliveryMethod = request.getParameter("deliveryMethod");
			order.setDeliveryMethod(deliveryMethod);

			// 判斷是否付款
			order.setPaymentStatus("貨到付款".equals(paymentMethod) ? "未付款" : "已付款");
			// 設定訂單狀態
			order.setOrderStatus("待出貨");

			// 處理多筆明細
			String[] bookIds = request.getParameterValues("bookId");
			String[] quantities = request.getParameterValues("quantity");
			String[] prices = request.getParameterValues("price");
			List<OrderItem> items = new ArrayList<>();

			if (bookIds != null) {
				for (int i = 0; i < bookIds.length; i++) {
					if (bookIds[i] != null && !bookIds[i].isEmpty()) {
						OrderItem item = new OrderItem();
						BooksBean book = new BooksBean();
						book.setBookId(Integer.parseInt(bookIds[i]));
						item.setBooksBean(book);
						item.setQuantity(Integer.parseInt(quantities[i]));
						item.setPrice(new BigDecimal(prices[i]));
						item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));// 數量也要轉成BigDecimal物件，否則方法無法相乘
						items.add(item);
					}
				}
			}

			orderService.insertOrder(order, items);
			model.addAttribute("order", order);// 資料掛載
			model.addAttribute("items", items);// 資料掛載
			return "order/InsertOrderDisplay";

		} catch (Exception e) {
			model.addAttribute("error", "建立訂單失敗: " + e.getMessage());
			return "order/InsertOrderDisplay";
		}
	}

	// 新增明細到既有訂單
	@PostMapping("/order/addItems")
	// 不使用Model，使用RedirectAttributes，因為model是導到新頁面，這裡是要新增完導回查詢單筆訂單頁面
	public String addItemsToOrder(@RequestParam("orderId") Integer orderId, HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		try {
			String[] bookIds = request.getParameterValues("bookId");
			String[] prices = request.getParameterValues("price");
			String[] quantities = request.getParameterValues("quantity");
			List<OrderItem> items = new ArrayList<>();

			if (bookIds != null) {
				for (int i = 0; i < bookIds.length; i++) {
					OrderItem item = new OrderItem();
					BooksBean book = new BooksBean();
					book.setBookId(Integer.parseInt(bookIds[i]));
					item.setBooksBean(book);
					item.setQuantity(Integer.parseInt(quantities[i]));
					item.setPrice(new BigDecimal(prices[i]));
					item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));// 數量也要轉成BigDecimal物件，否則方法無法相乘
					items.add(item);
				}
			}
			orderService.addItemsToOrder(orderId, items);
			redirectAttributes.addAttribute("id", orderId);
			return "redirect:/order/get";
		} catch (Exception e) {
			// addFlashAttribute傳遞臨時訊息，等到跳轉後的下一個頁面讀取完後，就會直接刪除
			redirectAttributes.addFlashAttribute("error", "新增明細失敗");
			return "redirect:/order/get?id=" + orderId;
		}
	}

	// ==================查詢類 Controller==================//

	// 查詢活動中的訂單
	@GetMapping("/order/activeList")
	public String listActiveOrders(Model model) {
		List<Orders> orders = orderService.getAllActiveOrders();
		model.addAttribute("orders", orders);
		return "order/GetAllOrders";
	}

	// 查詢已取消的訂單
	@GetMapping("/order/cancelledList")
	public String listCancelledOrders(Model model) {
		List<Orders> orders = orderService.getCancelledOrders();
		model.addAttribute("orders", orders);
		return "order/GetAllCancelOrders";
	}

	// 查詢單筆訂單明細
	@GetMapping("/order/get")
	public String getOrderById(@RequestParam("id") Integer orderId, Model model) {
		Orders order = orderService.getOrderById(orderId);
		if (order == null) {
			return "redirect:/order/activeList?error=OrderNotFound";
		}
		List<OrderItem> items = orderService.getOrderItemsByOrderId(orderId);
		model.addAttribute("order", order);
		model.addAttribute("items", items);
		return "order/GetOrder";
	}

	// 查詢單筆已取消訂單明細
	@GetMapping("/order/getCancel")
	public String getCancelOrderById(@RequestParam("id") Integer orderId, Model model) {
		Orders order = orderService.getOrderById(orderId);
		if (order == null) {
			return "redirect:/order/cancelledList?error=OrderNotFound";
		}
		List<OrderItem> items = orderService.getOrderItemsByOrderId(orderId);
		model.addAttribute("order", order);
		model.addAttribute("items", items);
		return "order/GetCancelOrder";
	}

	// 查詢所有明細
	@GetMapping("/order/allItems")
	public String listAllItems(Model model) {
		List<OrderItem> items = orderService.getAllOrderItems();
		model.addAttribute("items", items);
		return "order/GetAllOrderItems";
	}

	// ==================更新類 Controller==================//

	// 更新訂單主檔
	@PostMapping("/order/update")
	public String processUpdateOrder(@ModelAttribute Orders order, Model model) {
		try {
			orderService.updateOrder(order);
			model.addAttribute("order", order);
			return "order/UpdateOrderDisplay";
		} catch (Exception e) {
			return "redirect:/order/activeList";
		}
	}

	// 更新單筆明細
	@PostMapping("/order/updateItem")
	public String updateOrderItem(@ModelAttribute OrderItem item, @RequestParam("orderId") Integer orderId,
			Model model) {
		try {
			Orders order = new Orders();
			order.setOrderId(orderId);
			item.setOrders(order);

			orderService.updateOrderItem(item);
			model.addAttribute("orderId", orderId);
			return "order/UpdateOrderItemDisplay";
		} catch (Exception e) {
			return "redirect:/order/get?id=" + orderId;
		}
	}

	// ==================取消與還原類 Controller==================//
	// --- 3. 刪除與狀態切換類 Controller ---

	// 取消訂單-軟刪除
	@PostMapping("/order/cancel")
	public String cancelOrder(@RequestParam("id") Integer orderId) {
		orderService.processCancelOrder(orderId);
		return "redirect:/order/activeList";
	}

	// 刪除單筆明細
	@PostMapping("/order/deleteItem")
	public String deleteItem(@RequestParam("orderItemId") Integer orderItemId,
			@RequestParam("orderId") Integer orderId) {
		orderService.deleteOrderItem(orderItemId);
		return "redirect:/order/get?id=" + orderId;
	}

	// ==================其他 Controller==================//

	// 獲取書籍詳情回傳 JSON
	@GetMapping("/order/getBookDetail")
	@ResponseBody // ajax，不要找jsp
	public BooksBean getBookDetail(@RequestParam("bookId") Integer bookId) {
		return bookService.selectBookByIdS(bookId);
	}
	// ================= API Endpoints for Vue Frontend ================= //

	@GetMapping("/order/api/activeList")
	@ResponseBody
	public List<Orders> getActiveOrdersApi() {
		return orderService.getAllActiveOrders();
	}

	@GetMapping("/order/api/cancelledList")
	@ResponseBody
	public List<Orders> getCancelledOrdersApi() {
		return orderService.getCancelledOrders();
	}

	@GetMapping("/order/api/get")
	@ResponseBody
	public Map<String, Object> getOrderApi(@RequestParam("id") Integer orderId) {
		Map<String, Object> response = new java.util.HashMap<>();
		Orders order = orderService.getOrderById(orderId);
		if (order != null) {
			List<OrderItem> items = orderService.getOrderItemsByOrderId(orderId);
			// 取得退貨資訊
			OrderReturnBean returnInfo = orderService.getReturnRequestByOrderId(orderId);

			response.put("order", order);
			response.put("items", items);
			response.put("returnInfo", returnInfo);
		}
		return response;
	}

	@GetMapping("/order/api/userOrders")
	@ResponseBody
	public List<Orders> getUserOrdersApi(@RequestParam("userId") Integer userId) {
		return orderService.getOrdersByUserId(userId);
	}

	@PostMapping("/order/api/insert")
	@ResponseBody
	public Map<String, Object> insertOrderApi(HttpServletRequest request) {
		Map<String, Object> response = new java.util.HashMap<>();
		try {
			// 重用解析邏輯
			Orders order = new Orders();
			String userIdStr = request.getParameter("userId");
			if (userIdStr == null) {
				// Fallback if not provided in params, maybe try attribute or token util if
				// available?
				// For now assume param is sent.
				throw new Exception("User ID is required");
			}
			Integer userId = Integer.parseInt(userIdStr);
			UserBean user = new UserBean();
			user.setUserId(userId);
			order.setUserBean(user);
			order.setRecipientAt(request.getParameter("recipientName"));
			order.setAddress(request.getParameter("address"));
			order.setPhone(request.getParameter("phone"));
			String paymentMethod = request.getParameter("paymentMethod");
			order.setPaymentMethod(paymentMethod);

			String deliveryMethod = request.getParameter("deliveryMethod");
			order.setDeliveryMethod(deliveryMethod);
			order.setPaymentStatus("貨到付款".equals(paymentMethod) ? "未付款" : "已付款");
			order.setOrderStatus("待出貨");

			String couponIdStr = request.getParameter("couponId");
			if (couponIdStr != null && !couponIdStr.trim().isEmpty()) {
				order.setCouponId(Integer.parseInt(couponIdStr));
			}

			String[] bookIds = request.getParameterValues("bookId");
			String[] quantities = request.getParameterValues("quantity");
			String[] prices = request.getParameterValues("price");
			List<OrderItem> items = new ArrayList<>();

			if (bookIds != null) {
				for (int i = 0; i < bookIds.length; i++) {
					if (bookIds[i] != null && !bookIds[i].isEmpty()) {
						OrderItem item = new OrderItem();
						BooksBean book = new BooksBean();
						book.setBookId(Integer.parseInt(bookIds[i]));
						item.setBooksBean(book);
						item.setQuantity(Integer.parseInt(quantities[i]));
						item.setPrice(new BigDecimal(prices[i]));
						item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
						items.add(item);
					}
				}
			}

			orderService.insertOrder(order, items);
			response.put("success", true);
			response.put("order", order);
			response.put("items", items);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", e.getMessage());
		}
		return response;
	}

	@PostMapping("/order/api/update")
	@ResponseBody
	public String updateOrderApi(@ModelAttribute Orders order) {
		try {
			Orders existingOrder = orderService.getOrderById(order.getOrderId());
			if (existingOrder != null) {
				existingOrder.setRecipientAt(order.getRecipientAt());
				existingOrder.setPhone(order.getPhone());
				existingOrder.setAddress(order.getAddress());
				existingOrder.setPaymentMethod(order.getPaymentMethod());
				existingOrder.setPaymentStatus(order.getPaymentStatus());
				existingOrder.setOrderStatus(order.getOrderStatus());
				existingOrder.setTotalAmount(order.getTotalAmount());
				// 不設定 UserBean，保留現有的使用者關聯

				orderService.updateOrder(existingOrder);
				return "success";
			} else {
				return "error: Order not found";
			}
		} catch (Exception e) {
			return "error: " + e.getMessage();
		}
	}

	@PostMapping("/order/api/updateFull")
	@ResponseBody
	public Map<String, Object> updateFullOrderApi(@RequestBody OrderFullUpdateDTO dto) {
		Map<String, Object> response = new java.util.HashMap<>();
		try {
			orderService.updateFullOrder(dto);
			response.put("success", true);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", e.getMessage());
		}
		return response;
	}

	@PostMapping("/order/api/cancel")
	@ResponseBody
	public String cancelOrderApi(@RequestParam("id") Integer orderId) {
		orderService.processCancelOrder(orderId);
		return "success";
	}

	@PostMapping("/order/api/return")
	@ResponseBody
	public String returnOrderApi(@RequestBody OrderReturnRequest request) {
		try {
			orderService.processReturnOrder(request.getOrderId(), request.getReason(), request.getDescription());
			return "success";
		} catch (Exception e) {
			return "error: " + e.getMessage();
		}
	}

	@PostMapping("/order/api/deleteItem")
	@ResponseBody
	public String deleteItemApi(@RequestParam("orderItemId") Integer orderItemId) {
		try {
			orderService.deleteOrderItem(orderItemId);
			return "success";
		} catch (Exception e) {
			return "error: " + e.getMessage();
		}
	}

	@PostMapping("/order/api/updateItem")
	@ResponseBody
	public String updateItemApi(@ModelAttribute OrderItem item, @RequestParam("orderId") Integer orderId) {
		try {
			// 重新連結到訂單
			Orders order = new Orders();
			order.setOrderId(orderId);
			item.setOrders(order);

			// 重新計算小計，以此確保前端計算正確
			item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));

			orderService.updateOrderItem(item);
			return "success";
		} catch (Exception e) {
			return "error: " + e.getMessage();
		}
	}

	@PostMapping("/order/api/addItems")
	@ResponseBody
	public String addItemsApi(@RequestParam("orderId") Integer orderId, HttpServletRequest request) {
		try {
			String[] bookIds = request.getParameterValues("bookId");
			String[] prices = request.getParameterValues("price");
			String[] quantities = request.getParameterValues("quantity");
			List<OrderItem> items = new ArrayList<>();

			if (bookIds != null) {
				for (int i = 0; i < bookIds.length; i++) {
					OrderItem item = new OrderItem();
					BooksBean book = new BooksBean();
					book.setBookId(Integer.parseInt(bookIds[i]));
					item.setBooksBean(book);
					item.setQuantity(Integer.parseInt(quantities[i]));
					item.setPrice(new BigDecimal(prices[i]));
					item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
					items.add(item);
				}
			}
			orderService.addItemsToOrder(orderId, items);
			return "success";
		} catch (Exception e) {
			return "error: " + e.getMessage();
		}
	}

	@GetMapping("/order/api/analysis/books")
	@ResponseBody
	public List<BookSalesDTO> getSalesAnalysisApi(
			@RequestParam(value = "startDate", required = false) String startDateStr,
			@RequestParam(value = "endDate", required = false) String endDateStr) {
		java.sql.Timestamp start = parseTimestamp(startDateStr);
		java.sql.Timestamp end = parseTimestamp(endDateStr);
		return orderService.getTopSellingBooks(start, end);
	}

	@GetMapping("/order/api/analysis/revenue")
	@ResponseBody
	public BigDecimal getSalesRevenueApi(
			@RequestParam(value = "startDate", required = false) String startDateStr,
			@RequestParam(value = "endDate", required = false) String endDateStr) {
		java.sql.Timestamp start = parseTimestamp(startDateStr);
		java.sql.Timestamp end = parseTimestamp(endDateStr);
		return orderService.getSalesRevenue(start, end);
	}

	@GetMapping("/order/api/analysis/overview")
	@ResponseBody
	public bookstore.dto.SalesOverviewDTO getSalesOverviewApi(
			@RequestParam(value = "startDate", required = false) String startDateStr,
			@RequestParam(value = "endDate", required = false) String endDateStr) {
		java.sql.Timestamp start = parseTimestamp(startDateStr);
		java.sql.Timestamp end = parseTimestamp(endDateStr);
		return orderService.getSalesOverview(start, end);
	}

	@GetMapping("/order/api/analysis/homepage-books")
	@ResponseBody
	public List<BookSalesDTO> getHomepageTopBooksApi() {
		// Default to current month
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
		cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
		cal.set(java.util.Calendar.MINUTE, 0);
		cal.set(java.util.Calendar.SECOND, 0);
		java.sql.Timestamp start = new java.sql.Timestamp(cal.getTimeInMillis());

		// End of month (or just 'now' is fine as upper bound)
		java.sql.Timestamp end = new java.sql.Timestamp(System.currentTimeMillis());

		return orderService.getTopSellingBooksFull(start, end);
	}

	@GetMapping("/order/api/analysis/recent-trend")
	@ResponseBody
	public List<bookstore.dto.MonthlySalesDTO> getRecentSalesRevenueApi() {
		return orderService.getRecentSalesTrends();
	}

	private Timestamp parseTimestamp(String dateStr) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return null;
		}
		try {
			// 假設格式符合標準或長格式（若僅有日期則補上時間）
			if (dateStr.length() <= 10) {
				return Timestamp.valueOf(dateStr + " 00:00:00");
			}
			return Timestamp.valueOf(dateStr);
		} catch (Exception e) {
			return null;
		}
	}

}