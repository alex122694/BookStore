<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>修改會員資料</title>
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
    max-width: 600px;
    padding: 35px 45px;
    border: 1px solid #dcd5c7;
    border-radius: 6px;
    background-color: #ffffff;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

h1 {
    color: #7b5e47; 
    font-size: 26px;
    text-align: center;
    margin-bottom: 20px;
    border-bottom: 1px solid #e0d9c9;
    padding-bottom: 15px;
}

hr {
    border: 0;
    height: 1px;
    background-color: #e0d9c9;
    margin: 10px 0 30px 0; 
}

table {
    width: 100%;
    border-collapse: collapse;
    margin: 0 auto 20px auto;
}

td {
    padding: 10px 0;
}

td:first-child {
    width: 30%;
    text-align: right;
    padding-right: 20px;
}

label {
    font-weight: 500;
    color: #6d6d6d;
    font-size: 15px;
}

input[type="email"],
input[type="password"],
input[type="text"],
input[type="date"],
select {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #d0c8b9; 
    border-radius: 4px;
    background-color: #fefcf9; 
    color: #4a4a4a;
    font-size: 15px;
    height: 40px; 
    box-sizing: border-box;
    transition: border-color 0.3s, box-shadow 0.3s;
}

input:focus,
select:focus {
    border-color: #9fb89e; 
    outline: none;
    box-shadow: 0 0 5px rgba(159, 184, 158, 0.4);
}

input[disabled] {
    background-color: #f7f3e8; 
    cursor: default;
    color: #7b5e47; 
    font-weight: bold;
}

input[type="submit"] {
    width: 100%;
    max-width: 300px; 
    padding: 12px 20px;
    background-color: #a07d58; 
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 17px;
    font-weight: bold;
    letter-spacing: 1px;
    
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); 
    transition: all 0.2s ease-in-out; 
    margin-bottom: 15px; 
}

input[type="submit"]:hover {
    background-color: #926f4e; 
    transform: translateY(-2px); 
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.25); 
}

input[type="submit"]:active {
    transform: translateY(0); 
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
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
    <h1>會員資料修改</h1>
    <jsp:useBean id="user" scope="request" class="bookstore.bean.UserBean" />
    <form action="${pageContext.request.contextPath}/users/update" method="POST"> 
        <input type="hidden" name="userId" value="${user.userId}">
        <input type="hidden" name="status" value="${user.status}">
		<input type="hidden" name="points" value="${user.points}">
        <table>
            <tr>
                <td><label>會員編號 (ID):</label></td>
                <td><input type="text" disabled value="${user.userId}"></td>
            </tr>
            <tr>
                <td><label for="email">帳號 (Email):</label></td>
                <td><input type="email" id="email" name="email" value="${user.email}" required></td>
            </tr>
            <tr>
                <td><label for="password">密碼:</label></td>
                <td><input type="password" id="password" name="UserPwd" value="${user.userPwd}"></td>
            </tr>
            <tr>
                <td><label for="name">姓名:</label></td>
                <td><input type="text" id="name" name="userName" value="${user.userName}" required></td>
            </tr>
            <tr>
                <td><label for="gender">性別:</label></td>
                <td>
                    <select id="gender" name="gender">
                        <option value="">請選擇</option>
                        <option value="M" ${user.gender eq 'M' ? 'selected' : ''}>男</option>
                        <option value="F" ${user.gender eq 'F' ? 'selected' : ''}>女</option>
                    </select>
                </td>
            </tr>
            <tr><td><label for="birth">生日:</label></td>
                <td><input type="date" id="birth" name="birth" 
       				 value="<fmt:formatDate value='${user.birth}' pattern='yyyy-MM-dd'/>"></td></tr>
            <tr><td><label for="phone">聯絡電話:</label></td>
                <td><input type="text" id="phone" name="phoneNum" value="${user.phoneNum}"></td></tr>
            <tr><td><label for="address">地址:</label></td>
                <td><input type="text" id="address" name="address" value="${user.address}" size="50"></td></tr>
            <tr>
                <td><label for="userType">權限等級:</label></td>
                <td>
                    <select id="userType" name="userType">
                        <option value="">請選擇</option>
                        <option value="0" ${user.userType == 0 ? 'selected' : ''}>管理員</option>
                        <option value="1" ${user.userType == 1 ? 'selected' : ''}>一般會員</option>
                    </select>
                </td>
            </tr>
        </table>
        <br>
        <input type="submit" value="確認修改">
    </form>
    <br>
    <a href="${pageContext.request.contextPath}/users/list"><button>取消並返回列表</button></a>
</div>
</body>
</html>