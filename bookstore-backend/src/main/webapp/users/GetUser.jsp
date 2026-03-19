<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.*,bookstore.bean.UserBean"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>會員詳細資料</title>
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
    max-width: 550px;
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
    width: 35%;
    text-align: right;
    padding-right: 20px;
    color: #5d5d5d; 
    font-weight: 500;
}

input[type="text"][disabled] {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #dcd5c7; 
    border-radius: 4px;
    background-color: #f7f3e8; 
    color: #4a4a4a;
    font-size: 15px;
    height: 40px; 
    box-sizing: border-box;
    cursor: default; 
}

a button {
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
    margin-top: 15px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

a button:hover {
    background-color: #dcd5c7;
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

a button:active {
    transform: translateY(0);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
</head>
<body class="center-body">
<div align="center">
<h2>會員詳細資料</h2>

<jsp:useBean id="user" scope="request" class="bookstore.bean.UserBean" />

<table>
<tr><td>會員編號 (ID)
	<td><input type="text" disabled value="<%= user.getUserId() %>">
<tr><td>Email 帳號
	<td><input type="text" disabled value="<%= user.getEmail() %>">
<tr><td>會員姓名
	<td><input type="text" disabled value="<%= user.getUserName() %>">
<tr>
    <td>性別</td>
    <td>
        <% 
            String genderCode = user.getGender();
            String genderDisplay = "";
            
            if (genderCode != null) {
                if (genderCode.equals("M")) {
                    genderDisplay = "男";
                } else if (genderCode.equals("F")) {
                    genderDisplay = "女";
                } 
            }
        %>
        <input type="text" disabled value="<%= genderDisplay %>">
    </td>
</tr>
<tr><td>生日
	<td><input type="text" disabled value="<%= user.getBirth() %>">
<tr><td>聯絡電話
	<td><input type="text" disabled value="<%= user.getPhoneNum() %>">
<tr><td>地址
	<td><input type="text" disabled value="<%= user.getAddress() %>">
<tr><td>權限等級
	<td>
	    <% 
            Integer userType = user.getUserType();
            String userTypeDisplay = "";
            
            if (userType != null) {
                if (userType == 0) {
                    userTypeDisplay = "管理員";
                } else if (userType == 1) {
                	userTypeDisplay = "一般會員";
                } 
            }
        %>
        <input type="text" disabled value="<%= userTypeDisplay %>">
	</td>
</tr>	
</table>
<br>
<a href="${pageContext.request.contextPath}/users/list">
    <button>回到所有會員資料</button>
</a>
</div>
</body>
</html>