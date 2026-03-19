<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta charset="UTF-8">
        <title>購物車與訂單系統</title>
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
                width: 450px;
                max-width: 90%;
                padding: 35px 45px;
                border: 1px solid #dcd5c7;
                border-radius: 6px;
                background-color: #ffffff;
                box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
                text-align: center;
            }

            h2 {
                color: #7b5e47;
                font-size: 24px;
                margin-bottom: 30px;
                border-bottom: 1px solid #e0d9c9;
                padding-bottom: 15px;
                letter-spacing: 1px;
            }

            .button-group {
                display: flex;
                flex-direction: column;
                gap: 15px;
                margin-top: 20px;
                margin-bottom: 20px;
            }

            /* 通用按鈕基礎樣式 */
            button {
                width: 100%;
                height: 55px;
                padding: 10px 15px;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                font-size: 16px;
                font-weight: bold;
                letter-spacing: 1px;
                transition: background-color 0.3s, transform 0.2s, box-shadow 0.3s;
                box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
            }

            /* 查詢所有訂單按鈕 (主要動作) */
            .primary-action-button {
                background-color: #a07d58;
                color: white;
            }

            .primary-action-button:hover {
                background-color: #926f4e;
                transform: translateY(-2px);
                box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
            }

            .primary-action-button:active {
                background-color: #8c735d;
                transform: translateY(0);
                box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
            }

            /* 回到首頁按鈕 (輔助動作) */
            .back-button {
                background-color: #e8e4dc;
                color: #4a4a4a;
                font-weight: normal;
            }

            .back-button:hover {
                background-color: #dcd5c7;
                transform: translateY(-2px);
                box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
            }

            .back-button:active {
                background-color: #dcd5c7;
                transform: translateY(0);
                box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
            }

            /* 禁用按鈕樣式 */
            .btn-disabled {
                background-color: #d0d0d0;
                /* 柔和的灰色 */
                color: #7b7b7b;
                cursor: not-allowed;
                box-shadow: none;
                /* 移除陰影 */
                transform: none;
                transition: none;
                opacity: 0.8;
            }

            .btn-disabled:hover,
            .btn-disabled:active {
                background-color: #d0d0d0;
                transform: none;
                box-shadow: none;
            }
        </style>
    </head>

    <body class="center-body">
        <div align="center" class="container">
            <h2>請選擇功能</h2>

            <!-- 購物車按鈕 (無法跳轉) -->
            <div class="button-group">
                <!-- 購物車按鈕 -->
                <a href="<%=request.getContextPath()%>/cart">
                    <button class="primary-action-button">購物車系統</button>
                </a>

                <!-- 訂單系統按鈕 -->

                <a href="${pageContext.request.contextPath}/order/activeList">
                    <button class="primary-action-button">查詢活動訂單</button>
                </a>

                <a href="${pageContext.request.contextPath}/order/cancelledList">
                    <button class="primary-action-button">查詢取消/退貨訂單</button>
                </a>

                <br>
                <br>
                <a href="../users/home.jsp"><button class="back-button">返回首頁</button></a>
            </div>
    </body>

    </html>