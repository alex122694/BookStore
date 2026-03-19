<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.util.*, bookstore.bean.Orders, bookstore.bean.OrderItem"%>
<%!@SuppressWarnings("unchecked")%>
<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<title>新增訂單成功</title>
<style>
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

.container {
	width: 90%;
	max-width: 500px;
	padding: 35px 45px;
	border: 1px solid #dcd5c7;
	border-radius: 6px;
	background-color: #ffffff;
	box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
	text-align: center;
}

h2 {
	color: #7b5e47;
	font-size: 26px;
	margin-bottom: 20px;
	border-bottom: 1px solid #e0d9c9;
	padding-bottom: 15px;
}

h3 {
	color: #9fb89e; /* 青綠色強調成功 */
	font-size: 18px;
	margin-top: 30px;
	margin-bottom: 15px;
	padding-bottom: 5px;
	border-bottom: 1px dashed #9fb89e;
	font-weight: bold;
	text-align: left;
}

/* 訂單資訊區塊 */
.order-info {
	border: 1px solid #9fb89e; /* 青綠色邊框 */
	background-color: #f0f7f0; /* 淺綠色背景 */
	padding: 15px;
	margin-bottom: 20px;
	border-radius: 4px;
	text-align: left;
}

.order-info p {
	margin: 5px 0;
	font-size: 15px;
}

/* 訂單明細列表 */
ul {
	list-style-type: none; /* 移除點點 */
	padding: 0;
	margin-bottom: 30px;
	text-align: left;
	border: 1px solid #e0d9c9;
	padding: 15px;
	border-radius: 4px;
	background-color: #fefcf9;
}

ul li {
	padding: 5px 0;
	border-bottom: 1px dotted #e8e4dc;
	font-size: 15px;
}

ul li:last-child {
	border-bottom: none;
}

/* 按鈕樣式 (輔助動作) */
#btnBackToList {
	height: 40px;
	padding: 10px 20px;
	font-size: 16px;
	font-weight: normal;
	background-color: #e8e4dc;
	color: #4a4a4a;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	transition: background-color 0.2s, transform 0.1s, box-shadow 0.2s;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

#btnBackToList:hover {
	background-color: #dcd5c7;
	transform: translateY(-1px);
	box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

#btnBackToList:active {
	background-color: #dcd5c7;
	transform: translateY(0);
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
</head>

<body>
	<div align="center" class="container">
		<h2>新增訂單成功！</h2>

		<% Orders order=(Orders) request.getAttribute("order"); List<OrderItem> items = (List<OrderItem>)
                request.getAttribute("items");
                %>

		<div class="order-info">
			<p>
				訂單編號：
				<%= order.getOrderId() %>
			</p>
			<p>
				收件人：
				<%= order.getRecipientAt() %>
			</p>
			<p>
				總金額：
				<%= order.getTotalAmount() %>
			</p>
		</div>

		<h3>訂單明細</h3>
		<ul>
			<% if (items !=null) { for (OrderItem item : items) { %>
			<li>書籍ID: <%= item.getBooksBean().getBookId() %>, 數量: <%= item.getQuantity() %>,
				小計: <%= item.getSubtotal() %>
			</li>
			<% } } %>
		</ul>

		<button id="btnBackToList" class="btn">回到訂單列表</button>
	</div>

	<script src="https://code.jquery.com/jquery-3.7.1.js"></script>
	<script>
                    $(function () {
                        $("#btnBackToList").click(function () {
                            window.location.href = "<%= request.getContextPath() %>/order/activeList";
                        });
                    })
                </script>

</body>

</html>