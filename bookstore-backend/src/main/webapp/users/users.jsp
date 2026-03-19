<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
.center-body {
    font-family: 'Ã¥Â¾Â®Ã¨Â»ÂÃ¦Â­Â£Ã©Â»ÂÃ©Â«Â', 'Arial', sans-serif;
    background-color: #fcf8f0;
    color: #4a4a4a;
    margin: 0;
    padding: 40px 0; 
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

.main-title {
    color: #7b5e47; 
    font-size: 24px; 
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
    flex-direction: column; 
    gap: 15px; 
    margin-top: 20px;
}

.menu-link {
    text-decoration: none;
    width: 100%; 
}

.menu-button-small {
    width: 100%;
    height: 55px; 
    padding: 10px 15px;
    background-color: #a07d58;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
    letter-spacing: 1px;
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
    transition: background-color 0.3s, transform 0.2s, box-shadow 0.3s;
}

.menu-button-small:hover {
    background-color: #926f4e; 
    transform: translateY(-2px); 
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.menu-button-small:active {
    background-color: #8c735d; 
    transform: translateY(0); 
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1); 
}

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
</style>
<title>會員中心</title>
</head>
<body class="center-body">
<div align="center" class="container center-container">
	<h1 class="main-title">網路書店後台系統</h1>
    <h2 class="sub-title">會員中心</h2>
    <div class="button-group">
		<a href="${pageContext.request.contextPath}/users/list" class="menu-link"><button class="menu-button-small">查詢所有會員資料</button></a>
		<a href="home.jsp" class="menu-link"><button class="menu-button-small back-button">返回首頁</button></a>
	</div>
	<hr class="divider">
</div>
</body>
</html>