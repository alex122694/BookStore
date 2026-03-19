<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<title>修改訂單</title>
<style>
body {
	font-family: '微軟正黑體', 'Arial', sans-serif;
	background-color: #fcf8f0;
	color: #4a4a4a;
	margin: 0;
	padding: 40px 0;
	/* 修正：使用 flex column 讓頂層元素垂直堆疊並居中 */
	display: flex;
	flex-direction: column;
	align-items: center; /* 確保所有頂層元素水平居中 */
	min-height: 100vh;
}

/* 確保 h2 標題居中 */
h2 {
	color: #7b5e47;
	font-size: 26px;
	text-align: center;
	margin: 0 auto 20px auto; /* 確保它在 body 中居中 */
	max-width: 600px;
	border-bottom: 1px solid #e0d9c9;
	padding-bottom: 15px;
	width: 90%;
}

/* form 樣式保持不變，因為它已經有 margin: 0 auto; */
form {
	width: 90%;
	max-width: 600px;
	padding: 35px 45px;
	border: 1px solid #dcd5c7;
	border-radius: 6px;
	background-color: #ffffff;
	box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
	margin: 0 auto;
}

.form-group {
	margin-bottom: 15px;
}

label {
	display: block;
	margin-bottom: 5px;
	font-weight: 500;
	color: #6d6d6d;
	font-size: 15px;
}

/* Input Styles */
input[type="text"], select {
	width: 100%;
	padding: 10px 12px;
	border: 1px solid #d0c8b9;
	border-radius: 4px;
	box-sizing: border-box;
	background-color: #fefcf9;
	color: #4a4a4a;
	font-size: 15px;
	height: 40px;
	transition: border-color 0.3s, box-shadow 0.3s;
}

input:focus, select:focus {
	border-color: #9fb89e;
	outline: none;
	box-shadow: 0 0 5px rgba(159, 184, 158, 0.4);
}

/* Readonly/Disabled fields */
input[readonly] {
	background-color: #f7f3e8 !important;
	cursor: default;
	color: #7b5e47;
	font-weight: bold;
	border: 1px solid #dcd5c7;
}

/* Button Base Style */
.btn {
	padding: 10px 15px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 16px;
	font-weight: bold;
	transition: background-color 0.2s, transform 0.2s, box-shadow 0.2s;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	min-width: 120px;
}

/* 1. 確認修改按鈕 (Primary: 皮革棕) */
button[type="submit"] {
	background-color: #a07d58;
	color: white;
	box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
	margin-right: 10px;
}

button[type="submit"]:hover {
	background-color: #926f4e;
	transform: translateY(-2px);
	box-shadow: 0 6px 15px rgba(0, 0, 0, 0.25);
}

button[type="submit"]:active {
	transform: translateY(0);
	box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
}

/* 2. 取消按鈕 (Auxiliary: 淺土色) */
#btnCancel {
	background-color: #e8e4dc !important;
	color: #4a4a4a;
	font-weight: normal;
}

#btnCancel:hover {
	background-color: #dcd5c7 !important;
	transform: translateY(-1px);
	box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

#btnCancel:active {
	background-color: #dcd5c7 !important;
	transform: translateY(0);
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
</head>

<body>

	<h2>修改訂單</h2>

	<form action="<%= request.getContextPath() %>/order/update"
		method="post">
		<input type="hidden" name="userBean.userId" value="<%= request.getParameter("userId") %>">
		<div class="form-group">
			<label>訂單編號:</label> <input type="text" name="orderId"
				value="<%= request.getParameter("orderId") %>" readonly
				style="background-color: #eee;">
		</div>

		<div class="form-group">
			<label>訂單金額:</label> <input type="text" name="totalAmount"
				value="<%= request.getParameter("totalAmount") %>" readonly
				style="background-color: #eee;">
		</div>

		<div class="form-group">
			<label>收件人:</label> <input type="text" name="recipientAt"
				value="<%= request.getParameter("recipient") %>" required>
		</div>

		<div class="form-group">
			<label>地址:</label> <input type="text" name="address"
				value="<%= request.getParameter("address") %>" required>
		</div>

		<div class="form-group">
			<label>電話:</label> <input type="text" name="phone"
				value="<%= request.getParameter("phone") %>" required>
		</div>

		<div class="form-group">
			<label>付款方式:</label> <select name="paymentMethod">
				<option value="信用卡"
					<%="信用卡" .equals(request.getParameter("paymentMethod"))
                        ? "selected" : "" %>>信用卡</option>
				<option value="貨到付款"
					<%="貨到付款" .equals(request.getParameter("paymentMethod")) ? "selected" : "" %>>貨到付款</option>
				<option value="轉帳"
					<%="轉帳" .equals(request.getParameter("paymentMethod")) ? "selected"
                        : "" %>>轉帳</option>
			</select>
		</div>
		
		<div class="form-group">
			<label>付款狀態:</label> <select name="paymentStatus">
				<option value="未付款"
					<%="未付款" .equals(request.getParameter("paymentStatus"))
                        ? "selected" : "" %>>未付款</option>
				<option value="已付款"
					<%="已付款" .equals(request.getParameter("paymentStatus")) ? "selected" : "" %>>已付款</option>
				<option value="退款中"
					<%="退款中" .equals(request.getParameter("paymentStatus")) ? "selected"
                        : "" %>>退款中</option>
			</select>
		</div>

		<div class="form-group">
			<label>訂單狀態:</label> <select name="orderStatus">
				<option value="待出貨"
					<%="待出貨" .equals(request.getParameter("orderStatus")) ? "selected" : ""
                        %>>待出貨</option>
				<option value="已出貨"
					<%="已出貨" .equals(request.getParameter("orderStatus")) ? "selected" : ""
                        %>>已出貨</option>
				<option value="已到貨"
					<%="已到貨" .equals(request.getParameter("orderStatus")) ? "selected"
                        : "" %>>已到貨</option>
                <option value="已完成"
					<%="已完成" .equals(request.getParameter("orderStatus")) ? "selected"
                        : "" %>>已完成</option>        
				<option value="已取消"
					<%="已取消" .equals(request.getParameter("orderStatus")) ? "selected"
                        : "" %>>已取消</option>
                 <option value="退款中"
					<%="退款中" .equals(request.getParameter("orderStatus")) ? "selected"
                        : "" %>>退款中</option>
                <option value="已退款"
					<%="已退款" .equals(request.getParameter("orderStatus")) ? "selected"
                        : "" %>>已退款</option>        
			</select>
		</div>

		<button type="submit" class="btn">確認修改</button>
		<button type="button" id="btnCancel" class="btn">取消</button>
	</form>


	<script src="https://code.jquery.com/jquery-3.7.1.js"></script>
	<script>
            $(function () {
                $("#btnCancel").click(function () {
                    window.location.href = "<%= request.getContextPath() %>/order/activeList";
                });
            })
        </script>

</body>

</html>