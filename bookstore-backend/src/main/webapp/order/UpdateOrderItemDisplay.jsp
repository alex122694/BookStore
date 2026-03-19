<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta charset="UTF-8">
        <title>修改成功</title>
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

            .container {
                width: 90%;
                max-width: 500px;
                padding: 35px 45px;
                border: 1px solid #dcd5c7;
                border-radius: 6px;
                background-color: #ffffff;
                box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
                text-align: center;
                margin: 0 auto;
            }
            
            h2 {
                color: #7b5e47; 
                font-size: 26px;
                margin-bottom: 20px;
                border-bottom: 1px solid #e0d9c9;
                padding-bottom: 15px;
                width: 100%;
                box-sizing: border-box;
            }

            p {
                color: #9fb89e; /* 青綠色強調成功 */
                font-size: 16px;
                margin-top: 20px;
                margin-bottom: 30px;
                padding: 10px;
                border: 1px dashed #9fb89e;
                background-color: #f0f7f0;
                font-weight: bold;
                border-radius: 4px;
            }
            
            /* 按鈕樣式 (輔助動作) */
            #btnBackToDetail {
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

            #btnBackToDetail:hover {
                background-color: #dcd5c7;
                transform: translateY(-1px);
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
            }

            #btnBackToDetail:active {
                background-color: #dcd5c7;
                transform: translateY(0);
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            }
        </style>
    </head>

    <body>
	<div class="container">
        <h2>訂單明細修改成功！</h2>

        <p>您的變更已儲存，訂單總金額已重新計算。</p>

        <br>

        <button id="btnBackToDetail">返回訂單明細</button>
       </div>

        <script src="https://code.jquery.com/jquery-3.7.1.js"></script>
        <script>
            $(function () {
                $("#btnBackToDetail").click(function () {
                    window.location.href = "<%= request.getContextPath() %>/order/get?id=<%= request.getAttribute("orderId") %>";
                });
            })
        </script>

    </body>

    </html>