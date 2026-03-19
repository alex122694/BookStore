<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>新增評價完成</title>
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
    color: #9fb89e; /* 成功訊息使用柔和的青綠色 */
    font-size: 18px;
    text-align: center;
    margin: 20px 0;
    padding: 10px 0;
    border: 1px dashed #9fb89e;
    background-color: #f0f7f0;
    font-weight: bold;
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

/* Disabled Input, Select, Textarea Styles */
input[type="text"][disabled],
select[disabled],
textarea[disabled] {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #dcd5c7; 
    border-radius: 4px;
    background-color: #f7f3e8; /* 淺米色背景，模擬不可編輯 */
    color: #4a4a4a;
    font-size: 15px;
    box-sizing: border-box;
    cursor: default; 
    resize: none; 
}

input[type="text"][disabled],
select[disabled] {
    height: 40px; 
}

/* Return Button (Auxiliary Action) */
.form-action-group {
    text-align: center;
    margin-top: 25px; 
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
    <h1>新增評價完成</h1>
    
    <jsp:useBean id="review" scope="request" class="bookstore.bean.ReviewBean" />
    
        <table>
            <tr>
                <td><label for="userid">會員編號:</label></td>
                <td><input type="text" id="userid" name="user_id" value="<%= review.getUserId() %>" disabled></td>
            </tr>
            <tr>
                <td><label for="bookid">書本編號:</label></td>
                <td><input type="text" id="bookid" name="book_id" value="<%= (review.getBook() != null) ? review.getBook().getBookId() : review.getBookId() %>" disabled></td>
            </tr>
            
            <tr>
                <td><label for="rating">評分:</label></td>
                <td>
					 <input type="text" disabled value="<%= review.getRating() %>">
                </td>
            </tr>
            <tr>
                <td><label for="comment">評價:</label></td>
                <td><textarea id="comment" name="comment" rows="4" cols="40" disabled><%= review.getComment() %></textarea></td>
            </tr>
        </table>
        
        <br>
        
        <h3><%= request.getAttribute("message") %></h3>
        
    <div class="form-action-group">
        <a href="${pageContext.request.contextPath}/GetAllReviews">
            <button class="form-back-button">返回評價列表</button>
        </a>
    </div>

</div>
</body>
</html>