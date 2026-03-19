<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*, bookstore.bean.Orders"%>
<%!java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");%>
<%!@SuppressWarnings("unchecked")%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>已取消/退貨訂單紀錄</title>
<style>
/* ------------------ 基礎與表格樣式 (完全同步主畫面) ------------------ */
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
	width: 98%;
	max-width: 1600px;
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
}

table {
	width: 100%;
	border-collapse: collapse;
	margin: 20px 0;
	font-size: 13px; /* 欄位多，稍微調小字體 */
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
	text-align: center;
}

tbody tr:nth-child(even) {
	background-color: #f7f3f0;
}

tbody tr:hover {
	background-color: #fffaf0;
	transition: background-color 0.3s;
}

/* ------------------ 按鈕樣式 ------------------ */
.top-action-group {
	display: flex;
	gap: 10px;
	margin-bottom: 20px;
}

.btn {
	padding: 10px 18px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 15px;
	font-weight: 500;
	transition: all 0.2s;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

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

#btnBackActive {
	background-color: #a07d58;
	color: white;
	font-weight: bold;
	box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

#btnBackActive:hover {
	background-color: #926f4e;
	transform: translateY(-2px);
	box-shadow: 0 6px 12px rgba(0, 0, 0, 0.25);
}

#btnBackActive:active {
	background-color: #926f4e;
	transform: translateY(0);
	box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
}

.btn-detail {
	background-color: #9fb89e;
	color: #4a4a4a;
	width: 100px;
}

.btn-detail:hover {
	background-color: #8da68c;
	transform: translateY(-1px);
	box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
}

.btn-edit {
	background-color: #a07d58;
	color: white;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
	width: 100px;
}

.btn-edit:hover {
	background-color: #926f4e;
	transform: translateY(-1px);
	box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
}

.btn-edit:active {
	transform: translateY(0);
	box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* ------------------ MODAL 彈窗樣式 (大地色系化) ------------------ */
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
	background-color: #fffaf0; /* 奶油白背景 */
	margin: 10% auto;
	padding: 30px;
	border: 1px solid #dcd5c7;
	width: 320px;
	border-radius: 8px;
	text-align: center;
	box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.modal-icon {
	font-size: 48px;
	color: #b05252; /* 柔和的警告紅土色 */
	margin-bottom: 15px;
}

.modal-text {
	font-size: 16px;
	font-weight: bold;
	color: #4a4a4a;
	margin-bottom: 25px;
}

.modal-btn-cancel {
	background-color: #e8e4dc; /* 輔助色 */
	color: #4a4a4a;
}

/* 修正 modal 內部按鈕的基礎樣式 */
#modal-buttons .btn {
	padding: 8px 15px;
	font-size: 15px;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 狀態標籤 */
.status-cell {
	white-space: nowrap;
}
</style>
</head>
<body>
	<div align="center">
		<h2>已取消/退款訂單紀錄</h2>

		<div class="top-action-group">
			<button id="btnBack" class="btn">回到訂單系統</button>
			<button id="btnBackActive" class="btn">查詢活動訂單</button>
		</div>

		<table>
			<thead>
				<tr>
					<th>訂單編號</th>
					<th>用戶編號</th>
					<th>訂單金額</th>
					<th>付款方式</th>
					<th>付款狀態</th>
					<th>訂單狀態</th>
					<th>收件人</th>
					<th>收件地址</th>
					<th>聯絡電話</th>
					<th>寄件時間</th>
					<th>到貨時間</th>
					<th>取貨時間</th>
					<th>付款時間</th>
					<th>訂單建立時間</th>
					<th>最後修改時間</th>
					<th>操作</th>
				</tr>
			</thead>
			<tbody>
				<%
				List<Orders> orders = (List<Orders>) request.getAttribute("orders");
				if (orders != null && !orders.isEmpty()) {
					for (Orders order : orders) {
				%>
				<tr>
					<td><%=order.getOrderId()%></td>
					<td><%=order.getUserBean().getUserId()%></td>
					<td class="status-cell">$<%=order.getTotalAmount()%></td>
					<td class="status-cell"><%=order.getPaymentMethod()%></td>
					<td class="status-cell"><%=order.getPaymentStatus()%></td>
					<td class="status-cell"><%=order.getOrderStatus()%></td>
					<td><%=order.getRecipientAt()%></td>
					<td><%=order.getAddress()%></td>
					<td><%=order.getPhone()%></td>
					<td>
						<%-- 寄件時間 --%> <%
 if (order.getShippedAt() != null) {
 %> <%=sdf.format(order.getShippedAt())%> <%
 }
 %>
					</td>
					<td>
						<%-- 到貨時間 --%> <%
 if (order.getDeliveredAt() != null) {
 %> <%=sdf.format(order.getDeliveredAt())%> <%
 }
 %>
					</td>
					<td>
						<%-- 取貨時間 --%> <%
 if (order.getReceivedAt() != null) {
 %> <%=sdf.format(order.getReceivedAt())%> <%
 }
 %>
					</td>
					<td>
						<%-- 付款時間 --%> <%
 if (order.getPaidAt() != null) {
 %> <%=sdf.format(order.getPaidAt())%> <%
 }
 %>
					</td>
					<td>
						<%-- 訂單建立時間 --%> <%
 if (order.getCreatedAt() != null) {
 %> <%=sdf.format(order.getCreatedAt())%> <%
 }
 %>
					</td>
					<td>
						<%-- 訂單修改時間 --%> <%
 if (order.getUpdatedAt() != null) {
 %> <%=sdf.format(order.getUpdatedAt())%> <%
 }
 %>
					</td>
					<td>
						<button class="btn btn-detail" data-id="<%=order.getOrderId()%>">查詢明細</button>

						<%-- 只有狀態是「已取消」才顯示還原按鈕 --%> <%
 if ("已取消".equals(order.getOrderStatus())) {
 %>
						<button class="btn btn-edit btn-restore"
							data-id="<%=order.getOrderId()%>">還原訂單</button> <%
 }
 %>
					</td>
				</tr>
				<%
				}
				} else {
				%>
				<tr>
					<td colspan="16" style="text-align: center; padding: 30px;">目前沒有任何歷史紀錄。</td>
				</tr>
				<%
				}
				%>
			</tbody>
		</table>
	</div>
	<div id="custom-modal" class="modal">
		<div class="modal-content">
			<div id="modal-content-icon" class="modal-icon"></div>
			<p id="modal-content-text" class="modal-text"></p>
			<div id="modal-buttons"></div>
		</div>
	</div>

	<script src="https://code.jquery.com/jquery-3.7.1.js"></script>
	<script>
		// 1. 確認是否還原
		function showConfirmModal(message, confirmCallback) {
			// 設定內容
			$("#modal-content-icon").html('⚠'); // 警告圖標
			$("#modal-content-text").text(message);

			// 設定按鈕
			$("#modal-buttons")
					.html(
							'<button id="modal-btn-confirm" class="btn" style="background-color: #a07d58; color: white; margin-right: 10px;">確定</button>'
									+ '<button id="modal-btn-cancel" class="btn modal-btn-cancel">取消</button>');

			// 顯示彈窗
			$("#custom-modal").fadeIn();

			// 綁定事件
			$("#modal-btn-confirm").off('click').on('click', function() {
				$("#custom-modal").fadeOut();
				confirmCallback(true);
			});

			$("#modal-btn-cancel").off('click').on('click', function() {
				$("#custom-modal").fadeOut();
				confirmCallback(false);
			});
		}

		$(function() {
			// 回到上一頁 (CartAndOrder.jsp)
			$("#btnBack").click(function() {
				window.location.href = "${pageContext.request.contextPath}/order/CartAndOrder.jsp";
			});

			// 查詢活動訂單 (GetAllOrders)
			$("#btnBackActive").click(function() {
				window.location.href = "${pageContext.request.contextPath}/order/activeList";
			});

			// 查詢取消訂單明細
			$(".btn-detail").click(function() {
				let id = $(this).data("id");
				window.location.href = "${pageContext.request.contextPath}/order/getCancel?id=" + id;
			});

			// 還原訂單
			$(".btn-restore")
					.click(
							function() {
								let id = $(this).data("id");

								showConfirmModal(
										'確定要將此訂單還原為活動訂單嗎？',
										function(confirmed) {
											if (confirmed) {
												// 建立動態表單發送 POST 請求至 RestoreOrder Servlet
												let form = $('<form action="${pageContext.request.contextPath}/order/restore" method="post">'
														+ '<input type="hidden" name="id" value="' + id + '">'
														+ '</form>');
												$('body').append(form);
												form.submit();
											}
										});
							});
		});
	</script>
</body>
</html>