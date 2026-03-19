<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.*,bookstore.bean.UserBean" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<%!@SuppressWarnings("unchecked")%>
<html>
<head>
<meta charset="UTF-8">
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

    form {
        display: flex; 
        gap: 10px; 
        align-items: center;
        justify-content: center;
        margin-bottom: 20px;
    }
    
    input[type="text"],
    select {
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

    input[type="text"]:focus,
    select:focus {
        border-color: #9fb89e; 
        outline: none;
        box-shadow: 0 0 5px rgba(159, 184, 158, 0.4);
    }
    
    input[type="submit"] {
        height: 40px;
        padding: 10px 20px;
        background-color: #a07d58; 
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 16px;
        font-weight: bold;
        transition: background-color 0.3s, transform 0.1s;
    }

    input[type="submit"]:hover {
        background-color: #926f4e; 
        transform: translateY(-1px);
    }
    
    hr {
        border: 0;
        height: 1px;
        background-color: #e0d9c9;
        margin: 20px 0 30px 0; 
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

	.system-button {
	    height: 40px;
	    font-size: 16px;
	    padding: 10px 20px;
	    font-weight: bold;
	    transition: all 0.2s ease-in-out; 
	}
	
	
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
	}
</style>
<title>所有會員資料</title>
</head>
<body class="center-body">
<%
	Object typeFilterObj = request.getAttribute("currentUserTypeFilter");
	String typeValue = (typeFilterObj != null) ? typeFilterObj.toString() : "";

	String currentSearchName = (String) request.getAttribute("currentSearchName");
	String nameValue = (currentSearchName != null) ? currentSearchName : "";
%>
<div align="center">
<h2>所有會員資料</h2>
<div id="custom-alert" style="display:none; padding: 15px; margin-bottom: 20px; border-radius: 5px; 
		background-color: #f0f7f0; border: 1px solid #9fb89e;">
        <span id="alert-message" style="color: #4a4a4a; font-weight: bold;"></span>
        <button onclick="document.getElementById('custom-alert').style.display='none'" 
        style="float: right; background: none; border: 1px solid #ccc; color: #4a4a4a;
               line-height: 1; 
               vertical-align: middle; 
               padding: 5px 10px;
               ">X</button>
</div>
<form action="${pageContext.request.contextPath}/users/list" method="GET">
    <input type="text" name="searchName" placeholder="輸入「姓氏」進行查詢" 
           value="<%= nameValue %>">
    <select name="userTypeFilter">
        <option value="" <%= typeValue.equals("") ? "selected" : "" %>>顯示所有使用者</option>
        <option value="0" <%= typeValue.equals("0") ? "selected" : "" %>>僅顯示「管理員」</option>
        <option value="1" <%= typeValue.equals("1") ? "selected" : "" %>>僅顯示「一般會員」</option>
    </select>
    <input type="submit" value="查詢">
    <a href="${pageContext.request.contextPath}/users/list">
        <button type="button" class="system-button back-to-center-button">取消篩選</button>
    </a>
</form>
    
<table border="1">
<tr>
<th>會員編號<th>會員名稱<th>密碼<th>Email<th>性別<th>生日<th>電話號碼<th>地址<th>權限等級<th>修改資料<th>刪除資料
<%
List<UserBean> users = (List<UserBean>)request.getAttribute("users");
%>
	<%
	for(UserBean user: users) {
			String genderCode = user.getGender();
			String genderDisplay = "";
			if (genderCode != null) {
	            if (genderCode.equals("M")) {
	                genderDisplay = "男";
	            } else if (genderCode.equals("F")) {
	                genderDisplay = "女";
	            } else {
	                genderDisplay = genderCode;
	            }
	        }
			
		String userTypeDisplay = "一般會員";
	    	if (user.getUserType() != null && user.getUserType() == 0) {
	        	userTypeDisplay = "管理員";
	    	}
	%>
	<tr><td><%= user.getUserId() %>
	<td><a href="${pageContext.request.contextPath}/users/get?userId=<%= user.getUserId()%>"><%= user.getUserName() %></a>
	<td><%= user.getUserPwd() %>
	<td><%= user.getEmail() %>
	<td><%= genderDisplay %>
	<td><fmt:formatDate value="<%= user.getBirth() %>" pattern="yyyy-MM-dd" /></td>
	<td><%= user.getPhoneNum() %>
	<td><%= user.getAddress() %>
	<td><%= userTypeDisplay %>
	<td><a href="${pageContext.request.contextPath}/users/update?userId=<%= user.getUserId() %>">
	    <button class="update-button">修改</button>
	</a>
	<td><a href="${pageContext.request.contextPath}/users/delete?userId=<%= user.getUserId() %>" 
           onclick="return confirm('確定要刪除會員 「<%= user.getUserName() %>」 的資料嗎？');">
            <button class="delete-button">刪除</button>
        </a>
	
<% } %>	
</table>
<h3>共 <%= users.size() %> 筆會員資料</h3>
<a href="${pageContext.request.contextPath}/users/insert"><button class="system-button add-button">新增會員資料</button></a>
<a href="users.jsp"><button class="system-button back-to-center-button">回到會員中心首頁</button></a>
</div>

<script>
    const params = new URLSearchParams(window.location.search);
    const status = params.get('status');
    const message = params.get('msg'); 
    
    if (status === 'success' && message) {
        const customAlert = document.getElementById('custom-alert');
        const alertMessage = document.getElementById('alert-message');
        
        alertMessage.innerText = decodeURIComponent(message);
        customAlert.style.display = 'block';
        
        history.replaceState({}, document.title, window.location.pathname);
    }
</script>

</body>
</html>