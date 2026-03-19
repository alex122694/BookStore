<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<% 
    // 【修改點 1: JSP 邏輯】直接從 URL 參數取得 orderId
    String orderIdStr = request.getParameter("orderId");
    
    // 如果沒有 orderId，導向錯誤
    if (orderIdStr == null || orderIdStr.isEmpty()) {
        // 假設錯誤時導向訂單列表
        response.sendRedirect(request.getContextPath() + "/GetAllOrders");
        return;
    }
%>

<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<title>訂單 <%= orderIdStr %> 新增明細
</title>
<style>
body {
	font-family: '微軟正黑體', 'Arial', sans-serif;
	background-color: #fcf8f0;
	color: #4a4a4a;
	margin: 0;
	padding: 40px 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	min-height: 100vh;
}

/* 標題居中並定義最大寬度 */
h2 {
	color: #7b5e47;
	font-size: 26px;
	text-align: center;
	margin: 0 auto 20px auto;
	max-width: 600px;
	border-bottom: 1px solid #e0d9c9;
	padding-bottom: 15px;
	width: 90%;
}

.error-message {
    color: #b05252 !important; 
    background-color: #ffeaea;
    padding: 8px 15px;
    border: 1px dashed #e7c0c0;
    border-radius: 4px;
    
    /* 居中設定 */
    margin: 0 auto 20px auto; 
    text-align: center;
    width: 90%;
    max-width: 600px; /* 使用 form 的最大寬度，確保對齊 */
}

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

h3 {
	color: #7b5e47;
	font-size: 18px;
	margin-top: 30px;
	margin-bottom: 15px;
	padding-bottom: 5px;
	border-bottom: 1px dashed #e0d9c9;
}

h4 {
	color: #4a4a4a;
	font-size: 16px;
	margin-top: 0;
	margin-bottom: 15px;
	font-weight: bold;
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
input[type="text"], input[type="number"], select {
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

/* Readonly fields (單價 & 書名) */
input[readonly] {
	background-color: #eee; 
    cursor: default;
}


/* Item Row Container */
#items-container {
	padding: 10px;
	border: 1px solid #e0d9c9;
	border-radius: 4px;
	margin-top: 10px;
	margin-bottom: 20px;
}

.item-row {
	border: 1px solid #dcd5c7;
	padding: 15px;
	margin-bottom: 15px;
	background-color: #fffaf0;
	border-radius: 4px;
	position: relative;
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

/* 1. 確認新增明細按鈕 (Primary: 皮革棕) */
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

/* 2. 增加商品按鈕 (Secondary: 青綠色) */
#add-item-btn {
	background-color: #9fb89e !important;
	color: #4a4a4a;
	font-weight: normal;
}

#add-item-btn:hover {
	background-color: #8da68c !important;
	transform: translateY(-1px);
	box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

#add-item-btn:active {
	transform: translateY(0);
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 3. 取消按鈕 (Auxiliary: 淺土色) */
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

/* 4. 移除按鈕 (Warning: 柔和土紅) */
.remove-btn {
	background-color: #d89696 !important;
	color: white;
	padding: 5px 10px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	position: absolute;
	top: 15px;
	right: 15px;
	font-size: 12px;
	font-weight: normal;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.remove-btn:hover {
	background-color: #c48383 !important;
	transform: translateY(-1px);
	box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
}

.remove-btn:active {
	background-color: #c48383 !important;
	transform: translateY(0);
	box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* ------------------ MODAL 彈窗樣式 (從 InsertOrder.jsp 複製) ------------------ */
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
    color: #b05252; /* 警告紅 */
    margin-bottom: 15px;
}

.modal-text {
    font-size: 16px;
    font-weight: bold;
    color: #4a4a4a;
    margin-bottom: 25px;
}

.modal-btn-ok {
    background-color: #9fb89e !important; /* 綠色按鈕作為確認/好 */
    color: white;
}
/* 調整 modal 內部按鈕的基礎樣式 */
#modal-buttons button {
    padding: 8px 15px;
    font-size: 15px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    min-width: 80px; /* 統一按鈕寬度 */
}
</style>
</head>

<body>
	<h2>
		訂單編號
		<%= orderIdStr %>
		新增明細
	</h2>

	<% 
    String errorMessage = (String) request.getAttribute("error");
    if (errorMessage != null && !errorMessage.isEmpty()) {
	%>
	<p class="error-message">
		<%= errorMessage %>
	</p>
	<%}%>

	<form id="insertItemForm"
		action="<%= request.getContextPath() %>/order/addItems"
		method="post">

		<input type="hidden" name="orderId" value="<%= orderIdStr %>">

		<h3>商品資訊</h3>
		<div id="items-container"></div>

		<button type="button" id="add-item-btn" class="btn"
			style="margin-bottom: 20px;">+ 增加一筆商品</button>
		<br>
		<button type="submit" class="btn">確認新增明細</button>
		<button type="button" id="btnCancel" class="btn">取消</button>
	</form>

    <div id="custom-modal" class="modal">
		<div class="modal-content">
			<div id="modal-content-icon" class="modal-icon"></div>
			<p id="modal-content-text" class="modal-text"></p>
			<div id="modal-buttons"></div>
		</div>
	</div>

	<script src="https://code.jquery.com/jquery-3.7.1.js"></script>
	<script>
        // *** 【整合】顯示警告彈窗函式 (從 InsertOrder.jsp 複製) ***
        function showAlertModal(message) {
            $("#modal-content-icon").html('❌');
            $("#modal-content-text").text(message);

            $("#modal-buttons").html('<button id="modal-btn-ok" class="btn modal-btn-ok">確定</button>');

            $("#custom-modal").fadeIn();

            $("#modal-btn-ok").off('click').on('click', function() {
                $("#custom-modal").fadeOut();
            });
            
            $(window).off('click.modal').on('click.modal', function(event) {
                if (event.target.id === 'custom-modal') {
                    $("#custom-modal").fadeOut();
                    $(window).off('click.modal');
                }
            });
        }
        
        let itemCounter = 0; // 用於生成唯一的 ID 和 datalist 連結

        // 2. 函式：生成單筆商品明細 HTML
        function createItemRow() {
            itemCounter++;
            let index = itemCounter;
            
            // 移除 datalist，改用 Ajax 查詢
            return `
            <div class="item-row">
                <h4>商品
                    <button type="button" class="remove-btn">移除商品</button>
                </h4>
                
                <div class="form-group">
                    <label>書籍ID :</label>
                    <input type="text" name="bookId" placeholder="輸入 ID" 
                        class="book-id-input" required>
                </div>
                
                <div class="form-group">
                    <label>書籍名稱:</label>
                    <input type="text" name="bookName" class="item-name" readonly required >
                </div>
                
                <div class="form-group">
                    <label>單價:</label>
                    <input type="number" name="price" class="item-price" readonly required >
                </div>
                
                <div class="form-group">
                    <label>數量:</label>
                    <input type="number" name="quantity" value="1" min="1" required>
                </div>
            </div>
        `;
        }
        
        $(function () {
            // 取得 Context Path
            const contextPath = "<%= request.getContextPath() %>";

            // 初始加入一列
            $("#items-container").append(createItemRow());
            
            // 點擊新增按鈕
            $("#add-item-btn").click(function () {
                $("#items-container").append(createItemRow());
            });
            
            // *** 事件監聽邏輯：透過 Ajax 查詢名稱與單價 ***
            $("#items-container").on('change', '.book-id-input', function() {
                const $input = $(this);
                const selectedId = $input.val().trim(); 
                
                const $row = $input.closest('.item-row');
                const $nameInput = $row.find('.item-name');
                const $priceInput = $row.find('.item-price');

                // 清空舊的名稱和價格
                $nameInput.val('');
                $priceInput.val('');

                if (selectedId === "") {
                    return;
                }

                // 呼叫 /GetBookDetail Servlet
                $.ajax({
                    url: contextPath + "/order/getBookDetail", 
                    type: 'GET', 
                    data: { bookId: selectedId }, 
                    dataType: 'json',
                    success: function(response) {
                        if (response.error) {
                            // 處理找不到書籍或 ID 錯誤
                            showAlertModal("錯誤： " + response.error); // *** 【替換 alert】 ***
                            $input.val(''); 
                        } else {
                            // 成功取得資料，填入名稱和單價欄位
                            $nameInput.val(response.bookName);
                            $priceInput.val(response.price);
                        }
                    },
                    error: function(xhr, status, error) {
                        // 處理網路錯誤或伺服器錯誤
                        let errorMessage = "查詢書籍資料失敗，請檢查書籍 ID 是否輸入正確!";
                        if (xhr.responseJSON && xhr.responseJSON.error) {
                             errorMessage = "伺服器錯誤：" + xhr.responseJSON.error;
                        } else if (status === 'error' && xhr.status !== 0) {
                             errorMessage = `發生連線錯誤 (HTTP ${xhr.status})，請聯繫管理員。`;
                        }
                        
                        showAlertModal(errorMessage); // *** 【替換 alert】 ***
                        console.error("Ajax Error:", status, error);
                        $input.val(''); 
                    }
                });
            });
            
            // 移除商品明細邏輯
            $("#items-container").on('click', '.remove-btn', function() {
                $(this).closest('.item-row').remove();
            });

            // 取消按鈕 - 返回上一頁 (訂單明細頁)
            $("#btnCancel").click(function () {
                window.history.back(); // 這裡使用 window.history.back() 返回到呼叫頁面
            });
            
            // 提交表單時的最終檢查
            $("#insertItemForm").on('submit', function(e) {
                if ($(".item-row").length === 0) {
                    showAlertModal("請至少新增一筆訂單明細！"); // *** 【替換 alert】 ***
                    e.preventDefault();
                    return false;
                }
                
                // 檢查所有價格是否已帶入
                let allPricesSet = true;
                $('input[name="price"]').each(function() {
                    // 價格為空或為 0 都視為未成功帶入
                    if ($(this).val() === "" || $(this).val() === "0") {
                        allPricesSet = false;
                        return false; 
                    }
                });
                
                if (!allPricesSet) {
                    showAlertModal("請確認所有書籍 ID 皆已輸入並成功帶出價格！"); // *** 【替換 alert】 ***
                    e.preventDefault();
                    return false;
                }
            });
        });
    </script>

</body>

</html>