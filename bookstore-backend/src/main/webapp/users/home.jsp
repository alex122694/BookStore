<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
.dashboard-body {
    font-family: '微軟正黑體', 'Arial', sans-serif;
    background-color: #fcf8f0; 
    color: #4a4a4a;
    margin: 0;
    padding: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
}

.dashboard-container {
    width: 600px;
    max-width: 90%;
    padding: 35px 45px;
    border: 1px solid #dcd5c7;
    border-radius: 6px;
    background-color: #ffffff;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    text-align: center; 
}

.main-title {
    color: #7b5e47; 
    font-size: 26px;
    margin-bottom: 5px;
    border-bottom: 1px solid #e0d9c9;
    padding-bottom: 15px;
    letter-spacing: 1px;
}

.sub-title {
    color: #9c8470; 
    font-size: 19px;
    margin-top: 0;
    margin-bottom: 30px;
    font-weight: normal;
}

.divider {
    border: 0;
    height: 1px;
    background-color: #e0d9c9;
    margin-top: 35px;
}

.button-group {
    display: flex; 
    flex-wrap: wrap; 
    justify-content: center;
    gap: 15px; 
    margin-top: 20px;
}

.menu-link {
    text-decoration: none; 
    flex: 1 1 45%; 
    min-width: 200px; 
}

.menu-button {
    width: 100%;
    height: 90px; 
    padding: 10px 15px;
    background-color: #b0957b; 
    color: white;
    border: none;
    border-radius: 8px; 
    cursor: pointer;
    font-size: 18px;
    font-weight: bold;
    letter-spacing: 1px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    transition: background-color 0.3s, transform 0.2s, box-shadow 0.3s;
}

.menu-button:hover {
    background-color: #a5886d; 
    transform: translateY(-3px); 
    box-shadow: 0 8px 15px rgba(0, 0, 0, 0.15);
}

.menu-button:active {
    background-color: #92755e;
    transform: translateY(0);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.logout-button {
    height: 45px;
    padding: 0 30px;
    background-color: #e8e4dc;
    color: #6d6d6d;
    border: 1px solid #dcd5c7;
    border-radius: 4px;
    cursor: pointer;
    font-size: 16px;
    font-weight: normal;
    letter-spacing: 1px;
    transition: all 0.2s ease-in-out;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.logout-button:hover {
    background-color: #dcd5c7;
    color: #4a4a4a;
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.logout-button:active {
    background-color: #cec7b9;
    transform: translateY(0);
}

.logout-link {
    text-decoration: none;
}
</style>
<title>網路書店後台系統</title>
</head>

<body class="dashboard-body">
<div align="center" class="container dashboard-container">
    <h1 class="main-title">網路書店後台系統</h1>
    <h2 class="sub-title">功能選單</h2>
    <div class="button-group">
		<a href="users.jsp" class="menu-link"><button class="menu-button">會員中心</button></a>
		<a href="/books/booksIndex" class="menu-link"><button class="menu-button">書籍資料處理</button></a>
		<a href="../order/CartAndOrder.jsp" class="menu-link"><button class="menu-button">訂單與購物車系統</button></a>
		<a href="../reviews/review.jsp" class="menu-link"><button class="menu-button">評價管理</button></a>
</div>
<hr class="divider">
<div>
<a href="${pageContext.request.contextPath}/logout"><button class="logout-button">登出系統</button></a>
</div>
</div>
</body>
</html>