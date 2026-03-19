<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>新增評價資料</title>
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

input[type="text"],
select,
textarea {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #d0c8b9; 
    border-radius: 4px;
    background-color: #fefcf9; 
    color: #4a4a4a;
    font-size: 15px;
    box-sizing: border-box;
    transition: border-color 0.3s, box-shadow 0.3s;
    resize: vertical;
}

input[type="text"],
select {
    height: 40px; 
}

input:focus,
select:focus,
textarea:focus {
    border-color: #9fb89e; 
    outline: none;
    box-shadow: 0 0 5px rgba(159, 184, 158, 0.4);
}

/* Submit Button (Primary Action) */
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
    margin-bottom: 15px; /* 為下方的返回按鈕留出空間 */
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

/* Form Action Group and Back Button (Secondary Action) */
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
    <h1>新增評價資料</h1>
    
    <%
        String message = (String) request.getAttribute("message");
        if (message != null && !message.isEmpty()) {
    %>
        <p style="color: red;"><strong><%= message %></strong></p>
    <%
        }
    %>
    
    
    <form action="${pageContext.request.contextPath}/InsertReview" method="POST"> 
        
        <table>
            <tr>
                <td><label for="userid">會員編號:</label></td>
                <td><input type="text" id="userid" name="user_id" required placeholder="必填"></td>
            </tr>
            <tr>
                <td><label for="bookid">書籍編號:</label></td>
                <td><input type="text" id="bookid" name="book_id" required placeholder="必填"></td>
            </tr>
            
            <tr>
                <td><label for="rating">書本評分:</label></td>
                <td>
                    <select id="rating" name="rating">
                        <option value="">請選擇</option>
                        <option value="1">1</option>
                        <option value="2">2</option>
                        <option value="3">3</option>
                        <option value="4">4</option>
                        <option value="5">5</option>
                    </select>
                </td>
            </tr>
            
            <tr>
                <td><label for="comment">評價:</label></td>
                <td><textarea id="comment" name="comment" rows="4" cols="40"></textarea></td>
            </tr>
        </table>
        
        <br>
        
        <input type="submit" value="新增評價資料">
        
    </form>
    
    <div class="form-action-group">
        <a href="${pageContext.request.contextPath}/GetAllReviews">
            <button class="form-back-button">返回評價列表</button>
        </a>
    </div>

</div>
</body>
</html>