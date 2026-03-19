<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>查詢所有評價</title>
<style>
.center-body {
    font-family: '微軟正黑體', 'Arial', sans-serif;
    background-color: #fcf8f0;
    color: #4a4a4a;
    margin: 0;
    padding: 40px 0; /* 確保上下留白 */
    display: flex;
    justify-content: center;
    align-items: flex-start;
    min-height: 100vh;
}

.center-container {
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
}

.menu-link {
    text-decoration: none;
    width: 100%; 
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

/* 查詢全部評價按鈕 (主要動作) */
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

/* 返回首頁按鈕 (輔助動作) */
.back-button {
    background-color: #e8e4dc; /* 淺土色 */
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
</style>
</head>
<body class="center-body">
<div align="center" class="container center-container">
    <h2>查詢所有評價</h2>
    <div class="button-group">
<a href="<%= request.getContextPath() %>/GetAllReviews" class="menu-link">
    <button class="primary-action-button">查詢全部評價</button>
</a>
<a href="../users/home.jsp" class="menu-link"><button class="back-button">返回首頁</button></a>
</div>
</div>
</body>
</html>