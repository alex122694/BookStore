<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.*,bookstore.bean.ReviewBean,java.time.format.DateTimeFormatter"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>評價詳細資料</title>
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

div[align="center"] {
    width: 90%;
    max-width: 600px; /* 增加寬度以容納 textarea */
    padding: 35px 45px;
    border: 1px solid #dcd5c7;
    border-radius: 6px;
    background-color: #ffffff;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

h2 {
    color: #7b5e47; 
    font-size: 24px;
    text-align: center;
    margin-bottom: 30px;
    border-bottom: 1px solid #e0d9c9;
    padding-bottom: 10px;
}

table {
    width: 100%;
    border-collapse: collapse;
    margin: 20px auto;
}

tr {
    border-bottom: 1px dashed #e0d9c9; 
}

tr:last-child {
    border-bottom: none;
}

td {
    padding: 12px 0;
    font-size: 15px;
}

td:first-child {
    width: 30%;
    text-align: right;
    padding-right: 20px;
    color: #5d5d5d; 
    font-weight: 500;
}

/* Disabled Input/Textarea Styles (for display only) */
input[type="text"][disabled],
textarea[disabled] {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #dcd5c7; 
    border-radius: 4px;
    background-color: #f7f3e8; 
    color: #4a4a4a;
    font-size: 15px;
    box-sizing: border-box;
    cursor: default; 
    resize: none; /* 防止 textarea 拖動調整大小 */
}

input[type="text"][disabled] {
    height: 40px; 
}

/* Button Styles */
a button {
    height: 40px;
    padding: 10px 20px;
    font-size: 16px;
    font-weight: normal;
    
    /* 輔助動作色：淺土色 */
    background-color: #e8e4dc; 
    color: #4a4a4a;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    
    transition: background-color 0.2s, transform 0.1s, box-shadow 0.2s;
    margin-top: 15px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

a button:hover {
    background-color: #dcd5c7;
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

a button:active {
    background-color: #dcd5c7;
    transform: translateY(0); /* 按鈕下沉 */
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
</head>
<body class="center-body">
<div align="center">
<h2>評價詳細資料</h2>

<jsp:useBean id="review" scope="request" class="bookstore.bean.ReviewBean" />
<%
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
%>

<table>
<tr><td>評價編號:
	<td><input type="text" disabled value="<%= review.getReviewId() %>">
<tr><td>會員編號:
	<td><input type="text" disabled value="<%= review.getUserId() %>">
<tr><td>會員名稱:
	<td><input type="text" disabled value="<%= review.getUserName() %>">
<tr><td>書本編號:
	<td><input type="text" disabled value="<%= (review.getBook() != null) ? review.getBook().getBookId() : review.getBookId() %>">
<tr><td>書本名稱:
	<td><input type="text" disabled value="<%= review.getBookName() %>">
<tr><td>評分:
	<td><input type="text" disabled value="<%= review.getRating() %>">
<tr><td>評價:
	<td><textarea id="comment" name="comment" rows="4" cols="40" disabled><%= review.getComment() %></textarea></td>
<tr><td>創建時間:
	<td><input type="text" disabled value="<%= (review.getCreatedAt() == null ? "" : review.getCreatedAt().format(fmt)) %>">
</table>
<br>

<a href="${pageContext.request.contextPath}/GetAllReviews">
    <button>回到所有評價資料</button>
</a>

</div>
</body>
</html>