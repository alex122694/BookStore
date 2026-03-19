<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.util.*, bookstore.bean.Orders, bookstore.bean.OrderItem"%>
<%!@SuppressWarnings("unchecked")%>
<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<title>訂單明細</title>
<style>
/* ------------------ 基礎與頁面佈局 ------------------ */
body {
	font-family: '微軟正黑體', 'Arial', sans-serif;
	background-color: #fcf8f0;
	color: #4a4a4a;
	margin: 0;
	padding: 40px 0;
	display: flex;
	justify-content: center;
	align-items: flex-start;
	min-height: 100vh;
}

div[align="center"] {
	width: 95%;
	max-width: 1200px;
	padding: 30px;
	border: 1px solid #dcd5c7;
	border-radius: 6px;
	background-color: #ffffff;
	box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

h2 {
	color: #7b5e47;
	font-size: 24px;
	margin-bottom: 25px;
	border-bottom: 1px solid #e0d9c9;
	padding-bottom: 10px;
	text-align: center;
}

h3 {
	color: #7b5e47;
	font-size: 18px;
	margin-top: 30px;
	margin-bottom: 15px;
	padding-bottom: 5px;
	border-bottom: 1px dashed #e0d9c9;
	text-align: left;
}

/* 訂單主檔資訊區塊 */
.order-info-block {
	border: 1px solid #e0d9c9;
	background-color: #fffaf0;
	padding: 15px;
	margin-bottom: 20px;
	border-radius: 4px;
	display: flex;
	flex-wrap: wrap;
	gap: 5px 20px;
}

.order-info-block p {
	margin: 5px 0;
	font-size: 15px;
	flex: 1 1 30%; /* 每個段落佔約三分之一寬度 */
	min-width: 150px;
	color: #5d5d5d;
}

.order-info-block p strong {
	color: #4a4a4a;
	font-weight: bold;
}

/* ------------------ 表格樣式 ------------------ */
table {
	width: 100%;
	border-collapse: collapse;
	margin: 20px 0;
	font-size: 14px;
}

th, td {
	border: 1px solid #e0d9c9;
	padding: 10px 8px;
	text-align: left;
	vertical-align: middle;
}

th {
	background-color: #e8e4dc;
	color: #5d5d5d;
	font-weight: bold;
	letter-spacing: 0.5px;
}

tbody tr:nth-child(even) {
	background-color: #f7f3f0;
}

tbody tr:hover {
	background-color: #fffaf0;
	transition: background-color 0.3s;
}

/* ------------------ 按鈕群組樣式 ------------------ */
.action-group-top {
	display: flex;
	gap: 10px;
	margin-bottom: 20px;
}

/* 通用按鈕基礎樣式 */
button {
	padding: 10px 15px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 14px;
	font-weight: 500;
	transition: background-color 0.2s, transform 0.1s, box-shadow 0.2s;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	min-width: 80px;
}

/* 返回按鈕 (輔助色) */
#btnBack {
	background-color: #e8e4dc;
	color: #4a4a4a;
}

#btnBack:hover {
	background-color: #dcd5c7;
	transform: translateY(-1px);
	box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

#btnBack:active {
	background-color: #dcd5c7;
	transform: translateY(0);
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* ------------------ MODAL 彈窗樣式 ------------------ */
.modal {
	display: none;
	position: fixed;
	z-index: 1000;
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	overflow: auto;
	background-color: rgba(0, 0, 0, 0.4);
}

.modal-content {
	background-color: #fffaf0;
	margin: 15% auto;
	padding: 30px;
	border: 1px solid #dcd5c7;
	width: 300px;
	border-radius: 8px;
	text-align: center;
	box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.modal-icon p {
	font-size: 48px;
	color: #b05252;
	margin: 0 0 15px 0;
}

.modal-text {
	font-size: 16px;
	font-weight: bold;
	color: #4a4a4a;
	margin-bottom: 25px;
}

/* 調整 modal 內部按鈕的基礎樣式 */
#modal-buttons button {
	padding: 8px 15px;
	font-size: 15px;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

#modal-btn-cancel {
	background-color: #e8e4dc !important;
	color: #4a4a4a;
}
</style>
</head>

<body>
	<div align="center">
		<h2>訂單明細</h2>

		<%
		Orders order = (Orders) request.getAttribute("order");
		List<OrderItem> items = (List<OrderItem>) request.getAttribute("items");
		%>

		<div class="order-info-block">
			<p>
				訂單編號：
				<%=order.getOrderId()%>
			</p>
			<p>
				收件人：
				<%=order.getRecipientAt()%>
			</p>
			<p>
				地址：
				<%=order.getAddress()%>
			</p>
			<p>
				電話：
				<%=order.getPhone()%>
			</p>
			<p>
				總金額：
				<%=order.getTotalAmount()%>
			</p>
			<p>
				狀態：
				<%=order.getOrderStatus()%>
			</p>
		</div>
		<div class="action-group-top">
			<button id="btnBack">返回訂單列表</button>
		</div>
		<h3>商品列表</h3>
		<table>
			<thead>
				<tr>
					<th>訂單明細編號</th>
					<th>書籍編號</th>
					<th>單價</th>
					<th>數量</th>
					<th>小計</th>
				</tr>
			</thead>
			<tbody>
				<%
				if (items != null) {
					for (OrderItem item : items) {
				%>
				<tr>
					<td><%=item.getOrderItemId()%></td>
					<td><%=item.getBooksBean().getBookId()%></td>
					<td><%=item.getPrice()%></td>
					<td><%=item.getQuantity()%></td>
					<td><%=item.getSubtotal()%></td>
				</tr>
				<%
				}
				}
				%>
			</tbody>
		</table>

		<script src="https://code.jquery.com/jquery-3.7.1.js"></script>
		<script>
               $(function () {
                // 1. 返回按鈕
                  $("#btnBack").click(function () {
                    window.location.href = "<%=request.getContextPath()%>/order/cancelledList";
                  });
               });
        </script>
</body>

</html>