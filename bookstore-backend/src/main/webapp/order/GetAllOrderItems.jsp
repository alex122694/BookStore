<%@page import="bookstore.bean.OrderItem"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*, bookstore.bean.Orders"%>
<%!@SuppressWarnings("unchecked")%>
<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<title>所有訂單</title>
<style>
    /* ------------------ 基礎與表格樣式 ------------------ */
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
        margin: 0 auto;
    }

    h2 {
        color: #7b5e47; 
        font-size: 24px;
        margin-bottom: 25px;
        border-bottom: 1px solid #e0d9c9;
        padding-bottom: 10px;
        text-align: center;
    }

    table {
        width: 100%;
        border-collapse: collapse; 
        margin: 20px 0;
        font-size: 14px;
    }

    th,
    td {
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

    /* ------------------ 頂部按鈕群組樣式 ------------------ */
    .top-action-group {
        display: flex;
        gap: 10px;
        margin-bottom: 20px;
    }
    
    /* 通用按鈕基礎樣式 */
    .btn {
        padding: 10px 18px;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 15px;
        font-weight: 500;
        transition: background-color 0.2s, transform 0.1s, box-shadow 0.2s;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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

    /* 新增按鈕 (主要色) */
    #btnAddOrder {
        background-color: #a07d58;
        color: white;
        font-weight: bold;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2); 
    }
    
    #btnAddOrder:hover {
        background-color: #926f4e;
        transform: translateY(-2px);
        box-shadow: 0 6px 12px rgba(0, 0, 0, 0.25); 
    }
    
    #btnAddOrder:active {
        background-color: #926f4e;
        transform: translateY(0);
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
    }


    /* ------------------ 表格內按鈕樣式 ------------------ */

    /* 修改明細按鈕 (次要操作色 - 皮革棕) */
    .btn-edit {
        background-color: #a07d58;
        color: white;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
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

    /* 刪除明細按鈕 (警告色 - 土紅色) */
    .btn-del {
        background-color: #d89696;
        color: white;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }

    .btn-del:hover {
        background-color: #c48383;
        transform: translateY(-1px);
        box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
    }

    .btn-del:active {
        background-color: #c48383;
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
        background-color: rgba(0,0,0,0.4); 
    }

    .modal-content {
        background-color: #fffaf0; 
        margin: 15% auto; 
        padding: 30px;
        border: 1px solid #dcd5c7;
        width: 320px; 
        border-radius: 8px;
        text-align: center;
        box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
    }
    
    .modal-icon {
        font-size: 48px; 
        color: #b05252; 
        margin-bottom: 15px;
    }

    .modal-text {
        font-size: 16px;
        font-weight: bold;
        color: #4a4a4a;
        margin-bottom: 25px;
    }
    
    .modal-btn-cancel {
        background-color: #e8e4dc !important; 
        color: #4a4a4a;
    }

    /* 調整 modal 內部按鈕的基礎樣式 */
    #modal-buttons button {
        padding: 8px 15px;
        font-size: 15px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
    
</style>
</head>

<body>
	<div align="center">
	<h2>所有訂單明細</h2>

	<div class="top-action-group">
	<button id="btnBack" class="btn">回到上一頁</button>
	<button id="btnAddOrder" class="btn btn-add">新增訂單</button>
	</div>
	<table>
		<thead>
			<tr>
				<th>訂單明細編號</th>
				<th>訂單編號</th>
				<th>書籍編號</th>
				<th>單價</th>
				<th>數量</th>
				<th>小計</th>
				<th>操作</th>
				
			</tr>
		</thead>
		<tbody>
			<%
			List<OrderItem> items = (List<OrderItem>) request.getAttribute("items");
			if (items != null) {
				for (OrderItem item : items) {
			%>
			<tr>
				<td><%=item.getOrderItemId()%></td>
				<td><%=item.getOrders().getOrderId()%></td>
				<td><%=item.getBooksBean().getBookId()%></td>
				<td><%=item.getPrice()%></td>
				<td><%=item.getQuantity()%></td>
				<td><%=item.getSubtotal()%></td>
				<td>
					<button class="btn btn-edit btn-update-item"
						data-itemid="<%=item.getOrderItemId()%>"
						data-orderid="<%=item.getOrders().getOrderId()%>"
						data-bookid="<%=item.getBooksBean().getBookId()%>"
						data-price="<%=item.getPrice()%>"
						data-quantity="<%=item.getQuantity()%>">修改</button>

					<button class="btn btn-del btn-delete-item"
						data-itemid="<%=item.getOrderItemId()%>"
						data-orderid="<%=item.getOrders().getOrderId()%>">刪除</button>
				</td>
			</tr>
			<%
			}
			} else {
			%>
			<tr>
				<td colspan="7" style="text-align: center;">目前沒有訂單明細。</td>
			</tr>
			<%
			}
			%>
		</tbody>
	</table>

	<div id="custom-modal" class="modal">
		<div class="modal-content">
			<div id="modal-content-icon" class="modal-icon"></div>
			<p id="modal-content-text" class="modal-text"></p>
			<div id="modal-buttons"></div>
		</div>
	</div>
</div>


	<script src="https://code.jquery.com/jquery-3.7.1.js"></script>
	<script>
		// 顯示客製化確認彈窗的函式
		function showConfirmModal(message, confirmCallback) {
			// 設定內容
			$("#modal-content-icon").html('⚠'); // 警告圖標
			$("#modal-content-text").text(message);

			// 設定按鈕
			$("#modal-buttons")
					.html(
							'<button id="modal-btn-confirm" class="btn btn-del" style="margin-right: 10px;">確定</button>'
									+ '<button id="modal-btn-cancel" class="btn modal-btn-cancel">取消</button>');

			// 顯示彈窗
			$("#custom-modal").fadeIn();

			// 绑定事件
			$("#modal-btn-confirm").off('click').on('click', function() {
				$("#custom-modal").fadeOut();
				confirmCallback(true); // 執行確認回呼
			});

			$("#modal-btn-cancel").off('click').on('click', function() {
				$("#custom-modal").fadeOut();
				confirmCallback(false); // 執行取消回呼
			});
		}

		$(function() {
			$("#btnBack").click(function() {
				window.location.href = "${pageContext.request.contextPath}/order/activeList";
			});

			$("#btnAddOrder").click(function() {
				window.location.href = "${pageContext.request.contextPath}/order/InsertOrder.jsp";
			});

			$(".btn-detail").click(function() {
				let id = $(this).data("id");
				window.location.href = "/order/get?id=" + id;
			});

			 // 4. 修改明細按鈕 (保持不變)
            $(".btn-update-item").click(function () {
                let itemId = $(this).data("itemid");
                let orderId = $(this).data("orderid");
                let bookId = $(this).data("bookid");
                let price = $(this).data("price");
                let quantity = $(this).data("quantity");

                let url = "${pageContext.request.contextPath}/order/UpdateOrderItem.jsp?orderItemId=" + itemId +
                    "&orderId=" + orderId +
                    "&bookId=" + bookId +
                    "&price=" + price +
                    "&quantity=" + quantity;
                window.location.href = url;
            });

            // 5. 刪除單一明細按鈕(彈窗警告)
            $(".btn-delete-item").click(function () {
                let itemId = $(this).data("itemid");
                let orderId = $(this).data("orderid");

                showConfirmModal('確定要刪除此明細嗎？', function(confirmed) {
                    if (confirmed) {
                    	let form = $('<form action="${pageContext.request.contextPath}/order/deleteItem" method="post">' +
                            '<input type="hidden" name="orderItemId" value="' + itemId + '">' +
                            '<input type="hidden" name="orderId" value="' + orderId + '">' +
                            '</form>');
                        $('body').append(form);
                        form.submit();
                    }
                });
            });	
			
		});
	</script>

</body>

</html>