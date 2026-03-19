<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>修改評價資料</title>
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

/* Form Table Layout */
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

/* Input/Select/Textarea Styles */
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

/* Disabled field styles */
input[disabled] {
    background-color: #f7f3e8; 
    cursor: default;
    color: #7b5e47; 
    font-weight: bold;
    border: 1px solid #dcd5c7; 
}

/* Submit Button (Primary Action - Dynamic Style) */
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

/* Cancel Button (Secondary Action - Dynamic Style) */
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
    background-color: #dcd5c7;
    transform: translateY(0); 
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); 
}
</style>
</head>
<body class="center-body">

<div align="center">
    <h1>修改評價資料</h1>
    
    <jsp:useBean id="review" scope="request" class="bookstore.bean.ReviewBean" />
    
    
    <form action="${pageContext.request.contextPath}/UpdateReview" method="POST"> 
        
        <input type="hidden" name="review_id" value="${review.reviewId}">
        
        <input type="hidden" name="user_id" value="${review.userId}">
		<input type="hidden" name="book_id" value="${review.bookId}">
        
        <table>
            <tr>
                <td><label>評價編號:</label></td>
                <td><input type="text" disabled value="${review.reviewId}"></td>
            </tr>
            <tr>
                <td><label>會員編號:</label></td>
                <td><input type="text" disabled value="${review.userId}"></td>
            </tr>
            <tr>
                <td><label>會員名稱:</label></td>
                <td><input type="text" disabled value="${review.userName}"></td>
            </tr>
            <tr>
                <td><label>書本編號:</label></td>
                <td><input type="text" disabled value="${review.bookId}"></td>
            </tr>
            <tr>
                <td><label>書本名稱:</label></td>
                <td><input type="text" disabled value="${review.bookName}"></td>
            </tr>
            <tr>
                <td><label for="rating">評分:</label></td>
                <td>
                    <select id="rating" name="rating">
                        <option value="1" ${review.rating == 1 ? 'selected' : ''}>1</option>
                        <option value="2" ${review.rating == 2 ? 'selected' : ''}>2</option>
                        <option value="3" ${review.rating == 3 ? 'selected' : ''}>3</option>
                        <option value="4" ${review.rating == 4 ? 'selected' : ''}>4</option>
                        <option value="5" ${review.rating == 5 ? 'selected' : ''}>5</option>
                    </select>
                </td>
            </tr>
            <tr><td><label for="comment">評價:</label></td>
                <td><textarea id="comment" name="comment" rows="4" cols="40" required>${review.comment}</textarea></td></tr>
        </table>
        
        <br>
        <input type="submit" value="確認修改">
        
    </form>
    <br>
    
    <a href="${pageContext.request.contextPath}/GetAllReviews"><button>取消並返回列表</button></a>

</div>


</body>
</html>