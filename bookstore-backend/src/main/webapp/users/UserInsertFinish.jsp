<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>新增會員資料</title>
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

h3 {
	color: red;
}

p {
    text-align: center;
    margin-top: 20px;
}

p strong {
    color: #b05252;
    background-color: #ffeaea;
    padding: 8px 15px;
    border: 1px dashed #e7c0c0;
    border-radius: 4px;
    display: inline-block;
    margin-bottom: 15px;
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

p a {
    color: #9c8470; 
    text-decoration: none;
    font-size: 15px;
    transition: color 0.3s;
}

p a:hover {
    color: #7b5e47; 
    text-decoration: underline;
}

.form-action-group {
    text-align: center;
    margin-top: 15px; 
}

.form-back-button {
    height: 40px;
    padding: 10px 20px;
    font-size: 16px;
    font-weight: normal;
    background-color: #e8e4dc; 
    color: #4a4a4a;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.2s, transform 0.2s, box-shadow 0.2s;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); 
}

.form-back-button:hover {
    background-color: #dcd5c7;
    transform: translateY(-1px); 
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15); 
}

.form-back-button:active {
    background-color: #dcd5c7;
    transform: translateY(0); 
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); 
}
</style>
</head>
<body>
<div align="center">
    <h1>新增會員資料</h1>
    <jsp:useBean id="user" scope="request" class="bookstore.bean.UserBean" />
        <table>
            <tr>
                <td><label for="email">帳號 (Email):</label></td>
                <td><input type="email" id="email" name="email" value="<%= user.getEmail() %>" disabled></td>
            </tr>
            <tr>
                <td><label for="password">密碼:</label></td>
                <td><input type="password" id="password" name="user_pwd" value="<%= user.getUserPwd() %>" disabled></td>
            </tr>
            <tr>
                <td><label for="name">姓名:</label></td>
                <td><input type="text" id="name" name="user_name" value="<%= user.getUserName() %>" disabled></td>
            </tr>
            <tr>
                <td><label for="gender">性別:</label></td>
                <td>
                <%
                    String selectedGender = user.getGender() != null ? user.getGender() : ""; 
                %>
                    <select id="gender" name="gender" disabled>
                        <option value="">請選擇</option>
                        <option value="M" <%= selectedGender.equals("M") ? "selected" : "" %>>男</option>
                        <option value="F" <%= selectedGender.equals("F") ? "selected" : "" %>>女</option>
                    </select>
                </td>
            </tr>
            <tr>
    			<td><label for="birth">生日:</label></td>
    			<td>
        			<input type="date" id="birth" name="birth" 
               			value="<fmt:formatDate value='${user.birth}' pattern='yyyy-MM-dd'/>" disabled>
    			</td>
			</tr>
            <tr>
                <td><label for="phone">聯絡電話:</label></td>
                <td><input type="text" id="phone" name="phone_num" value="<%= user.getPhoneNum() %>" disabled></td>
            </tr>
            <tr>
                <td><label for="address">地址:</label></td>
                <td><input type="text" id="address" name="address" value="<%= user.getAddress() %>" disabled></td>
            </tr>
            <tr>
                <td><label for="userType">權限等級:</label></td>
                
                <td>
                    <select id="userType" name="user_type" disabled>
                        <option value="">請選擇</option>
                        <option value="0" <%= (user.getUserType() != null && user.getUserType() == 0) ? "selected" : "" %>>管理員</option>
                        <option value="1" <%= (user.getUserType() != null && user.getUserType() == 1) ? "selected" : "" %>>一般會員</option>
                    </select>
                </td>
            </tr>
        </table>
        <br>
        <h3><%= request.getAttribute("message") %></h3>
    <div class="form-action-group">
    <a href="${pageContext.request.contextPath}/users/list"><button class="form-back-button">返回會員列表</button></a>
	</div>
</div>
</body>
</html>