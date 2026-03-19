<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html>
<head>
<meta charset="UTF-8">
<title>書籍資料首頁</title>

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

div {
	width: 90%;
	max-width: 450px;
	padding: 35px 45px;
	box-sizing: border-box;
	border: 1px solid #dcd5c7;
	border-radius: 6px;
	background-color: #ffffff;
	box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
	text-align: center;
}

.main-title {
	color: #7b5e47;
	font-size: 26px;
	margin: 0 0 30px 0;
	padding-bottom: 15px;
	border-bottom: 1px solid #e0d9c9;
	font-weight: bold;
}

form {
	margin: 20px 0 15px 0;
}

a {
	text-decoration: none;
	display: block;
}

button, input[type="submit"] {
	width: 80%;
	max-width: 260px;
	height: 45px;
	padding: 10px 0;
	box-sizing: border-box;
	border-radius: 4px;
	border: none;
	font-size: 16px;
	letter-spacing: 0.1em;
	font-weight: bold;
	cursor: pointer;
	transition: background-color 0.2s, transform 0.2s, box-shadow 0.2s;
	box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
	margin: 0 auto;
}

.primary-button {
	background-color: #a07d58;
	color: white;
	box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.primary-button:hover {
	background-color: #926f4e;
	transform: translateY(-2px);
	box-shadow: 0 6px 15px rgba(0, 0, 0, 0.25);
}

.primary-button:active {
	transform: translateY(0);
	box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
}

.back-button {
	background-color: #e8e4dc;
	color: #4a4a4a;
	font-weight: normal;
}

.back-button:hover {
	background-color: #dcd5c7;
	transform: translateY(-1px);
	box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.back-button:active {
	background-color: #dcd5c7;
	transform: translateY(0);
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
</head>

<body>
	<div>
		<h2 class="main-title">書籍資料處理</h2>
		<form method="get" action="/books/getAllBooks">
			<input type="submit" value="書籍管理" class="primary-button">
		</form>
		<form method="get" action="/users/home.jsp">
			<input type="submit" value="返回首頁" class="back-button">
		</form>
	<!-- > 	<a href="../users/home.jsp"><button class="back-button">返回首頁</button></a>-->
		<!-- 
		<a href="../users/home.jsp" class="menu-link"><button class="menu-button-small back-button">返回首頁</button></a>
		 -->

	</div>
	<script type="text/javascript">
		
	</script>
</body>
</html>
