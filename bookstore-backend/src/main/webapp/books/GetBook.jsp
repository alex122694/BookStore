<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="bookstore.bean.BooksBean"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>書籍資料</title>

<style>
/* ------------------ 基礎與頁面佈局 ------------------ */
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

/* 中央白色卡片 */
div.container {
    width: 90%;
    max-width: 700px;
    padding: 35px 45px;
    box-sizing: border-box;
    border: 1px solid #dcd5c7;
    border-radius: 6px;
    background-color: #ffffff;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    text-align: center;
}

/* 標題 */
.h2-st1 {
    color: #7b5e47; 
    font-size: 26px;
    margin: 0 0 30px 0;
    padding-bottom: 15px;
    border-bottom: 1px solid #e0d9c9;
    font-weight: bold;
}

/* ------------------ 表格樣式 ------------------ */
table {
    width: 100%;
    border-collapse: collapse;
    margin: 0 auto 20px auto;
    font-size: 15px;
}

tr {
    border-bottom: 1px dashed #e0d9c9;
}

tr:last-child {
    border-bottom: none;
}

td {
    padding: 12px 0;
}

.field-label {
    width: 120px;
    text-align: right;
    white-space: nowrap;
    font-weight: 500;
    color: #6d6d6d;
    padding-right: 20px;
}

.field-value {
    text-align: left;
}

/* ------------------ 輸入框樣式 ------------------ */

/* 唯讀輸入框樣式 */
.readonly-input {
    width: 100%;
    box-sizing: border-box;
    padding: 10px 12px;
    border-radius: 4px;
    border: 1px solid #dcd5c7;
    background-color: #f7f3e8; /* 淺米色背景 */
    color: #4a4a4a;
    font-size: 15px;
    font-weight: bold;
    height: 40px;
    cursor: default;
}

/* ------------------ 簡述展開區塊 (Details/Summary) ------------------ */
.desc-details {
    margin: 0;
}

.desc-details>summary {
    list-style: none;
    cursor: pointer;
    display: inline-block;
    padding: 6px 12px;
    border-radius: 4px;
    
    /* 大地色系風格 */
    background-color: #a07d58; /* 皮革棕 */
    color: #fff;
    font-size: 14px;
    letter-spacing: 0.05em;
    user-select: none;
    transition: background-color 0.2s;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.desc-details>summary::-webkit-details-marker {
    display: none;
}

.desc-details[open]>summary,
.desc-details>summary:hover {
    background-color: #926f4e; /* 懸停/展開時顏色變深 */
}

.desc-content {
    margin-top: 8px;
    padding: 15px;
    border-radius: 4px;
    background-color: #fefcf9; /* 淺色內容背景 */
    border: 1px solid #e0d9c9;
    max-height: 150px;
    overflow-y: auto;
    line-height: 1.6;
    white-space: pre-wrap;
    text-align: left;
    color: #5d5d5d;
}

/* ------------------ 按鈕樣式 (輔助動作) ------------------ */
form {
    margin-top: 30px;
}

input[type="submit"] {
    width: 50%;
    max-width: 250px;
    height: 45px;
    padding: 12px 0;
    box-sizing: border-box;
    
    /* 輔助動作樣式 */
    background-color: #e8e4dc; /* 淺土色 */
    color: #4a4a4a;
    border: none;
    border-radius: 4px;
    font-size: 16px;
    font-weight: normal;
    letter-spacing: 0.1em;
    
    cursor: pointer;
    transition: background-color 0.2s, transform 0.2s, box-shadow 0.2s;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* hover 效果 */
input[type="submit"]:hover {
    background: #dcd5c7;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
    transform: translateY(-1px);
}

/* 按下效果 */
input[type="submit"]:active {
    background: #dcd5c7;
    transform: translateY(0);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>

</head>
<body>
	<div class="container">
		<h2 class="h2-st1">書籍資料</h2>

		<jsp:useBean id="book" scope="request" class="bookstore.bean.BooksBean" />

		<table>
			<tr>
				<td class="field-label">書籍編號：</td>
				<td class="field-value"><input type="text"
					class="readonly-input" disabled value="<%=book.getBookId()%>">
				</td>
			</tr>
			<tr>
				<td class="field-label">書籍名稱：</td>
				<td class="field-value"><input type="text"
					class="readonly-input" disabled value="<%=book.getBookName()%>">
				</td>
			</tr>
			<tr>
				<td class="field-label">作者：</td>
				<td class="field-value"><input type="text"
					class="readonly-input" disabled value="<%=book.getAuthor()%>">
				</td>
			</tr>
			<tr>
				<td class="field-label">譯者：</td>
				<td class="field-value"><input type="text"
					class="readonly-input" disabled value="<%=book.getTranslator()%>">
				</td>
			</tr>
			<tr>
				<td class="field-label">出版社：</td>
				<td class="field-value"><input type="text"
					class="readonly-input" disabled value="<%=book.getPress()%>">
				</td>
			</tr>
			<tr>
				<td class="field-label">ISBN：</td>
				<td class="field-value"><input type="text"
					class="readonly-input" disabled value="<%=book.getIsbn()%>">
				</td>
			</tr>
			<tr>
				<td class="field-label">價錢：</td>
				<td class="field-value"><input type="text"
					class="readonly-input" disabled value="<%=book.getPrice()%>">
				</td>
			</tr>
			<tr>
				<td class="field-label">庫存量：</td>
				<td class="field-value"><input type="text"
					class="readonly-input" disabled value="<%=book.getStock()%>">
				</td>
			</tr>
			<tr>
				<td class="field-label">建立日期：</td>
				<td class="field-value"><input type="text"
					class="readonly-input" disabled
					value="<%=(book.getCreatedAt() != null) ? book.getCreatedAt().toLocalDate() : ""%>">
				</td>
			</tr>
			<tr>
				<td class="field-label">簡述：</td>
				<td class="field-value"><details class="desc-details">
						<summary>展開 / 收合 簡述內容</summary>
						<div class="desc-content">
							<%=(book.getShortDesc() != null) ? book.getShortDesc() : ""%>
						</div>
					</details></td>
			</tr>
		</table>
				<form action="/books/getAllBooks" method="GET">
					<input type="submit" value="查看所有書籍">
				</form>
		</div>
</body>
</html>
