<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<title>新增訂單</title>
<style>
/* ... (原有的 CSS 樣式保持不變) ... */
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

h2 {
	color: #7b5e47;
	font-size: 26px;
	text-align: center;
	margin: 0 auto 20px auto;
	max-width: 700px;
	border-bottom: 1px solid #e0d9c9;
	padding-bottom: 15px;
	width: 90%;
}

p[style="color:red"] {
	color: #b05252 !important;
	background-color: #ffeaea;
	padding: 8px 15px;
	border: 1px dashed #e7c0c0;
	border-radius: 4px;
	margin: 0 auto 20px auto;
	text-align: center;
	width: 90%;
	max-width: 700px;
}

form {
	width: 90%;
	max-width: 700px;
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

/* 特別為唯讀欄位設定樣式 */
input[readonly] {
	background-color: #eee;
	cursor: default;
}

input:focus, select:focus {
	border-color: #9fb89e;
	outline: none;
	box-shadow: 0 0 5px rgba(159, 184, 158, 0.4);
}

#items-container {
	padding: 10px;
	border: 1px solid #e0d9c9;
	border-radius: 4px;
	margin-top: 10px;
}

.item-row {
	border: 1px solid #dcd5c7;
	padding: 15px;
	margin-bottom: 15px;
	background-color: #fffaf0;
	border-radius: 4px;
	position: relative;
}

.btn {
	padding: 10px 15px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	text-decoration: none;
	font-size: 16px;
	font-weight: bold;
	transition: background-color 0.2s, transform 0.2s, box-shadow 0.2s;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	min-width: 120px;
}

button[type="submit"] {
	background-color: #a07d58;
	color: white;
	box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

button[type="submit"]:hover {
	background-color: #926f4e;
	transform: translateY(-2px);
	box-shadow: 0 6px 15px rgba(0, 0, 0, 0.25);
}

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

/* ------------------ MODAL 彈窗樣式 (從 GetAllOrders.jsp 複製) ------------------ */
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

	<h2>新增訂單</h2>

	<p style="color: red">
		<%= request.getAttribute("error") !=null ? request.getAttribute("error") : "" %>
	</p>

	<form action="<%= request.getContextPath() %>/order/insert"
		method="post">

		<h3>收件資訊</h3>
		<div class="form-group">
			<label for="userId">使用者ID:</label> <input type="text" id="userId"
				name="userId" required>
		</div>
		<div class="form-group">
			<label for="recipientName">收件人姓名:</label> <input type="text"
				id="recipientName" name="recipientName" required>
		</div>
		<div class="form-group">
			<label for="phone">電話:</label> <input type="text" id="phone"
				name="phone" required>
		</div>
		<div class="form-group">
			<label for="address">地址:</label> <input type="text" id="address"
				name="address" required>
		</div>
		<div class="form-group">
			<label for="paymentMethod">付款方式:</label> <select id="paymentMethod"
				name="paymentMethod" required>
				<option value="信用卡">信用卡</option>
				<option value="貨到付款">貨到付款</option>
				<option value="轉帳">轉帳</option>
			</select>
		</div>

		<h3>商品資訊</h3>
		<div id="items-container"></div>

		<button type="button" id="add-item-btn" class="btn"
			style="margin-bottom: 20px;">+ 增加一筆商品</button>
		<br>
		<button type="submit" class="btn">送出訂單</button>
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
        // *** 【新增】顯示警告彈窗函式 (取代 alert) ***
        function showAlertModal(message) {
            // 設定內容
            $("#modal-content-icon").html('❌'); // 錯誤/叉叉圖標
            $("#modal-content-text").text(message);

            // 設定按鈕：單一確定按鈕
            $("#modal-buttons")
                    .html(
                            '<button id="modal-btn-ok" class="btn modal-btn-ok">確定</button>'
                    );

            // 顯示彈窗
            $("#custom-modal").fadeIn();

            // 綁定事件：點擊確定或背景關閉
            $("#modal-btn-ok").off('click').on('click', function() {
                $("#custom-modal").fadeOut();
            });
            
            // 點擊背景也關閉
            $(window).off('click.modal').on('click.modal', function(event) {
                if (event.target.id === 'custom-modal') {
                    $("#custom-modal").fadeOut();
                    $(window).off('click.modal');
                }
            });
        }
        
        // 用來追蹤商品數量，並給予動態的編號
        let itemCounter = 0; 
        
        $(function () {
            // 取得 Context Path
            const contextPath = "<%= request.getContextPath() %>";

            // *** 修正後的 createItemRow 函式：新增書籍名稱欄位 ***
            function createItemRow() {
                const index = itemCounter; 
                itemCounter++; 

                return `
                    <div class="item-row" data-index="${index}">
                        <h4>商品
                            <button type="button" class="remove-btn">移除商品</button>
                        </h4>
                        <div class="form-group">
                            <label>書籍ID:</label>
                            <input type="text" name="bookId" placeholder="輸入書籍 ID" 
                                class="book-id-input" data-index="${index}" required>
                        </div>
                        
                        <div class="form-group">
                            <label>書籍名稱:</label>
                            <input type="text" name="bookName" value="" class="item-name" readonly required>
                        </div>
                        
                        <div class="form-group">
                            <label>單價:</label>
                            <input type="number" name="price" value="" class="item-price" readonly required>
                        </div>
                        <div class="form-group">
                            <label>數量:</label>
                            <input type="number" name="quantity" value="1" min="1" required>
                        </div>
                    </div>
                `;
            }

            // 初始加入一列
            $("#items-container").append(createItemRow());
            
            // 點擊新增按鈕
            $("#add-item-btn").click(function () {
                $("#items-container").append(createItemRow());
            });
            
            // 移除按鈕的事件監聽邏輯
            $("#items-container").on('click', '.remove-btn', function() {
                $(this).closest('.item-row').remove();
            });

            // *** 核心變動：事件監聽邏輯：透過 Ajax 查詢名稱與單價 ***
            $("#items-container").on('change', '.book-id-input', function() {
                const $input = $(this);
                const selectedId = $input.val().trim(); 
                
                // 找到這一行商品列
                const $row = $input.closest('.item-row');
                
                // 找到該列的名稱和單價輸入框
                const $nameInput = $row.find('.item-name');
                const $priceInput = $row.find('.item-price');

                // 清空舊的名稱和價格
                $nameInput.val('');
                $priceInput.val('');

                if (selectedId === "") {
                    return; // 如果為空，則不做查詢
                }

                // URL 路徑改為 /GetBookDetail
                $.ajax({
                    url: contextPath + "/order/getBookDetail", 
                    type: 'GET', 
                    data: { bookId: selectedId }, 
                    dataType: 'json',
                    success: function(response) {
                        if (response.error) {
                            // 處理找不到書籍或 ID 錯誤的情況
                            showAlertModal("錯誤： " + response.error); // *** 【替換 alert】 ***
                            $input.val(''); 
                        } else {
                            // 成功取得資料，填入名稱和單價欄位
                            $nameInput.val(response.bookName);
                            $priceInput.val(response.price);
                        }
                    },
                    error: function(xhr, status, error) {
                        // 處理網路錯誤或伺服器錯誤 (HTTP 狀態碼非 200)
                        let errorMessage = "查詢書籍資料失敗，請檢查書籍 ID 是否輸入正確!";
                        if (xhr.responseJSON && xhr.responseJSON.error) {
                            // 如果伺服器有回傳 JSON 錯誤訊息 (例如 HTTP 400/500)
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

            // 取消按鈕的邏輯
            $("#btnCancel").click(function () {
                window.location.href = contextPath + "/order/activeList";
            });
        })
    </script>

</body>

</html>