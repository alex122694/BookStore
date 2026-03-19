<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.*,bookstore.bean.ReviewBean,java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<%!@SuppressWarnings("unchecked")%>
<html>
<head>
<meta charset="UTF-8">
<title>所有評價</title>
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
        width: 95%;
        max-width: 1200px;
        padding: 30px;
        border: 1px solid #dcd5c7;
        border-radius: 6px;
        background-color: #ffffff;
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    }

    h2 {
        color: #7b5e47; 
        font-size: 24px;
        margin-bottom: 25px;
        border-bottom: 1px solid #e0d9c9;
        padding-bottom: 10px;
    }
    
    h3 {
        color: #9c8470; 
        font-weight: normal;
        margin-top: 30px;
        margin-bottom: 20px;
    }
    
    table {
        width: 100%;
        border-collapse: collapse; 
        margin: 20px 0;
        font-size: 15px;
    }
    
    th, td {
        border: 1px solid #e0d9c9; 
        padding: 12px 10px;
        text-align: left;
    }
    
    th {
        background-color: #e8e4dc; 
        color: #5d5d5d;
        font-weight: bold;
        letter-spacing: 0.5px;
    }
    
    tr:nth-child(even) {
        background-color: #f7f3f0; 
    }

    tr:hover {
        background-color: #fffaf0; 
        transition: background-color 0.3s;
    }

    td a {
        color: #a07d58; 
        text-decoration: none;
        transition: color 0.3s;
    }
    
    td a:hover {
        color: #7b5e47; 
        text-decoration: underline;
    }

    button {
        padding: 8px 15px;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 14px;
        font-weight: 500;
        transition: background-color 0.3s, transform 0.1s;
        margin: 5px; 
    }
    
    table td:nth-last-child(2),
	table td:last-child {
    	width: 1%; /* 告訴瀏覽器使用最小寬度 */
    	white-space: nowrap; /* 確保 '修改' 和 '刪除' 的標題不換行 */
	}

	table th:nth-last-child(2),
	table th:last-child {
    	width: 1%; 
    	white-space: nowrap;
	}

    table button {
        margin: 0 3px; 
        display: inline-block; 
    }

    /* 表格內的修改按鈕 */
    .update-button {
        background-color: #9fb89e; 
        color: #4a4a4a;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }

    .update-button:hover {
        background-color: #8da68c;
        transform: translateY(-1px);
        box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
    }

    .update-button:active {
        transform: translateY(0);
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    }
    
    /* 表格內的刪除按鈕 */
    .delete-button {
        background-color: #d89696; 
        color: white;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }

    .delete-button:hover {
        background-color: #c48383;
        transform: translateY(-1px);
        box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
    }

    .delete-button:active {
        background-color: #c48383;
        transform: translateY(0);
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    }

	/* 表格底下的按鈕群組 */
	.system-button {
	    height: 40px;
	    font-size: 16px;
	    padding: 10px 20px;
	    font-weight: bold;
	    transition: all 0.2s ease-in-out; 
        margin: 0 10px;
	}
	
	/* 新增評價按鈕 (主要動作) */
	.add-button {
	    background-color: #a07d58; 
	    color: white;
	    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); 
	}
	
	.add-button:hover {
	    background-color: #926f4e; 
	    transform: translateY(-2px); 
	    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.25); 
	}
	
	.add-button:active {
	    transform: translateY(0); 
	    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
	}
	
	/* 回到評價首頁按鈕 (輔助動作) */
	.back-to-center-button {
	    background-color: #e8e4dc; 
	    color: #4a4a4a;
	    font-weight: normal;
	    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); 
	}
	
	.back-to-center-button:hover {
	    background-color: #dcd5c7;
	    transform: translateY(-1px); 
	    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
	}
	
	.back-to-center-button:active {
	    transform: translateY(0);
	    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	}
	
	.status-cell {
    	white-space: nowrap; /* 強制文字在儲存格內保持在單行 */
    	min-width: 80px; /* 給予足夠的最小寬度，避免空間不足時仍換行 */
	}
</style>
</head>
<body class="center-body">
<div align="center">
<h2>所有評價</h2>
<table border="1">
<tr style="background-color:#a8fefa">
<th class="status-cell">評價編號<th class="status-cell">會員編號<th class="status-cell">會員名稱<th class="status-cell">書本編號<th>書本名稱<th class="status-cell">評分<th>評價<th>創建日期<th>修改資料<th>刪除資料
<%
List<ReviewBean> reviews = (List<ReviewBean>)request.getAttribute("reviews");
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
%>
	<%
	for(ReviewBean review: reviews) {
	%>
	<tr><td><a href="GetReview?reviewId=<%= review.getReviewId()%>"><%= review.getReviewId() %></a></td>
	<td><%= review.getUserId() %></td>
	<td><%= review.getUserName() %></td>
	<td><%= (review.getBook() != null) ? review.getBook().getBookId() : review.getBookId() %></td>
	<td><%= review.getBookName() %></td>
<%--<td><%= review.getUserId() %><a href="GetUser?userId=<%= user.getUserId()%>"><%= user.getUserId() %></a> --%>	
<%--<td><%= review.getBookId() %><a href="GetBook?bookId=<%= book.getBookId()%>"><%= book.getBookId() %></a> --%>	
	<td><%= review.getRating() %></td>
	<td><%= review.getComment() %></td>
	<td><%= (review.getCreatedAt() == null ? "" : review.getCreatedAt().format(fmt)) %></td>
	<td><a href="<%= request.getContextPath() %>/UpdateReview?review_id=<%= review.getReviewId() %>">
	    <button class="update-button">修改</button>
	</a>
	</td>
	<td><a href="<%= request.getContextPath() %>/DeleteReview?review_id=<%= review.getReviewId() %>" 
           onclick="return confirm('確定要刪除會員 [<%= review.getUserId() %>] 的評價嗎？');">
            <button class="delete-button">刪除</button>
        </a>
    </td>
	</tr>
<% } %>	
</table>
<h3>共 <%= reviews.size() %> 筆評價資料</h3>
<a href="reviews/ReviewInsert.jsp"><button class="system-button add-button">新增評價資料</button></a>
<a href="reviews/review.jsp"><button class="system-button back-to-center-button">回到評價首頁</button></a>
</div>

<script>
    const params = new URLSearchParams(window.location.search);
    const status = params.get('status');
    const message = params.get('msg'); 
    
    if (status === 'success' && message) {
        alert(decodeURIComponent(message));
        history.replaceState({}, document.title, window.location.pathname);
    }
</script>

</body>
</html>