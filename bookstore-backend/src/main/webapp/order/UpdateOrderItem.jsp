<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta charset="UTF-8">
        <title>修改訂單明細</title>
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

            h2 {
                color: #7b5e47; 
                font-size: 26px;
                text-align: center;
                margin: 0 auto 20px auto; 
                max-width: 450px; /* 調整標題最大寬度 */
                border-bottom: 1px solid #e0d9c9;
                padding-bottom: 15px;
                width: 90%;
            }

            form {
                width: 90%;
                max-width: 350px; 
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
            input[type="text"],
            input[type="number"] {
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
            
            input:focus {
                border-color: #9fb89e; 
                outline: none;
                box-shadow: 0 0 5px rgba(159, 184, 158, 0.4);
            }

            /* Readonly fields (書籍 ID) */
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
            
            /* 按鈕群組容器：用於居中按鈕 */
            .button-actions {
                display: flex;
                justify-content: center; /* 按鈕居中 */
                gap: 15px; /* 按鈕間距 */
                margin-top: 15px;
            }
            
            /* 1. 確認修改按鈕 (Primary: 皮革棕) */
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

        <h2>修改訂單明細</h2>

        <form action="<%= request.getContextPath() %>/order/updateItem" method="post">
            <input type="hidden" name="orderItemId" value="<%= request.getParameter("orderItemId") %>">
            <input type="hidden" name="orderId" value="<%= request.getParameter("orderId") %>">

            <div class="form-group">
                <label>書籍 ID:</label>
                <input type="text" name="booksBean.bookId" value="<%= request.getParameter("bookId") %>" readonly
                style="background-color: #eee;">
            </div>

            <div class="form-group">
                <label>單價:</label>
                <input type="number" name="price" value="<%= request.getParameter("price") %>" required>
            </div>

            <div class="form-group">
                <label>數量:</label>
                <input type="number" name="quantity" value="<%= request.getParameter("quantity") %>" min="1" required>
            </div>
			
			<div class="button-actions">
            <button type="submit" class="btn">確認修改</button>
            <button type="button" id="btnCancel" class="btn">取消</button>
            </div>
        </form>


        <script src="https://code.jquery.com/jquery-3.7.1.js"></script>
        <script>
            $(function () {
                $("#btnCancel").click(function () {
                    window.location.href = "<%= request.getContextPath() %>/order/get?id=<%= request.getParameter("orderId") %>";
                });
            })
        </script>

    </body>

    </html>