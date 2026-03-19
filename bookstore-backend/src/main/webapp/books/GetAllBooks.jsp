<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="bookstore.bean.BooksBean"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" type="text/css"
	href="https://cdn.datatables.net/2.0.7/css/dataTables.dataTables.min.css">
<meta charset="UTF-8">
<title>書籍資料</title>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/bookTable.css">
</head>
<body>
	<div id="mainDiv">
		<h2 class="h2-st1">書籍資料</h2>

		<div id="action-bar">

			<form action="<%=request.getContextPath()%>/books/insertPage"
				method="get">
				<input type="submit" value="新增書籍資料" class="btn-insert">
			</form>


		</div>
		<table class="books-table">
			<thead>
				<tr>
					<th style="text-align: center;">書籍編號</th>
					<th style="text-align: left;">書籍名稱</th>
					<th style="text-align: center;">作者</th>
					<th style="text-align: center;">譯者</th>
					<th style="text-align: center;">出版社</th>
					<th style="text-align: center;">類型</th>
					<th style="text-align: center;">價錢</th>
					<th style="text-align: center;">ISBN</th>
					<th style="text-align: center;">庫存</th>
					<th style="text-align: center;">修改</th>
					<th style="text-align: center;">封存</th>
					<th style="text-align: center;">銷售狀態</th>
				</tr>
			</thead>

			<tbody>
				<%
				List<BooksBean> bookList = (ArrayList<BooksBean>) request.getAttribute("bookList");
				%>
				<%
				if (bookList != null) {
					for (BooksBean book : bookList) {
				%>
				<tr>
					<td style="text-align: center;"><%=book.getBookId()%></td>
					<td style="text-align: left;"><a
						href="/books/getBook?bookId=<%=book.getBookId()%>"><%=book.getBookName()%></a></td>
					<td style="text-align: center;"><%=book.getAuthor()%></td>
					<td style="text-align: center;"><%=(book.getTranslator() == null || book.getTranslator().isEmpty()) ? "" : book.getTranslator()%></td>
					<td style="text-align: center;"><%=book.getPress()%></td>
					<td style="text-align: center;">
						<%
						%>
					</td>
					<td style="text-align: center;"><%=book.getPrice().intValue()%></td>
					<td style="text-align: center;"><%=book.getIsbn()%></td>
					<td style="text-align: center;"><%=book.getStock()%></td>
					<td style="text-align: center;">
						<%
						// 先取出狀態並處理 null
						Integer statusObj = book.getOnShelf();
						int shelfStatus = (statusObj == null) ? 0 : statusObj;
						boolean isArchived = (shelfStatus == 2);
						%>
						<form action="/books/updatePage" method="get">
							<input type="hidden" name="bookId" value="<%=book.getBookId()%>">
							<input type="submit" value="修改"
								class="btn btn-edit <%=isArchived ? "btn-disabled" : ""%>"
								<%=isArchived ? "disabled" : ""%>>
						</form>
					</td>
					<td style="text-align: center;">
						<%
						Integer currentStatus = book.getOnShelf();
						%>
						<button type="button"
							class="btn <%=(currentStatus != null && currentStatus == 2) ? "btn-secondary" : "btn-danger"%> archive-btn"
							data-bookId="<%=book.getBookId()%>"
							data-status="<%=currentStatus%>">
							<%=(currentStatus != null && currentStatus == 2) ? "解封" : "封存"%>
						</button>
					</td>
					<td style="text-align: center;">
						<div class="switch-container">
							<label class="book-switch"> <input type="checkbox"
								class="on-shelf-toggle" data-bookId="<%=book.getBookId()%>"
								<%=(book.getOnShelf() == 1) ? "checked" : ""%>
								<%=(book.getOnShelf() == 2) ? "disabled" : ""%>> <span
								class="slider"></span>
							</label><span class="status-label"> 
    <%
    Integer currentShelfStatus = book.getOnShelf();
    if (currentShelfStatus != null && currentShelfStatus == 1) {
        out.print("上架中");
    } else if (currentShelfStatus != null && currentShelfStatus == 2) {
        out.print("已封存");
    } else {
        out.print("下架中");
    }
    %>
</span>
						</div>
					</td>
				</tr>
				<%
				}
				} else {
				%>
				<tr>
					<td colspan="11">目前沒有任何書籍資料</td>
				</tr>
				<%
				}
				%>
			</tbody>
		</table>
		<h3>
			共
			<%=bookList.size()%>
			筆資料
		</h3>
		<!-- 
		<a href="/books/booksIndex" class="back-to-index-button"><button class="menu-button">書籍資料處理</button></a>
		 -->
		<form method="get" action="/books/booksIndex">
			<input type="submit" class="back-to-index-button" value="返回書籍首頁">
		</form>

	</div>
	<script src="https://code.jquery.com/jquery-3.6.0.js"></script>
	<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
	<script src="https://cdn.datatables.net/2.0.7/js/dataTables.min.js"></script>

	<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

	<script>
window.onload = function() {
    // 使用 EL 表達式檢查是否存在 insertBook
    <%if (request.getAttribute("insertBook") != null || session.getAttribute("insertBook") != null) {%>
        
        Swal.fire({
            title: '🎉 ${msg}',
            html: `
                <div style="display: flex; justify-content: center; padding: 10px 0;">
                    <table style="text-align: left; font-size: 15px; color: #4a4a4a;">
                        <tr>
                            <td style="font-weight: bold; width: 80px; vertical-align: top;">書名</td>
                            <td style="width: 20px; vertical-align: top;">：</td>
                            <td>${insertBook.bookName}</td>
                        </tr>
                        <tr>
                            <td style="font-weight: bold; vertical-align: top;">作者</td>
                            <td style="vertical-align: top;">：</td>
                            <td>${insertBook.author}</td>
                        </tr>
                        <tr>
                            <td style="font-weight: bold; vertical-align: top;">譯者</td>
                            <td style="vertical-align: top;">：</td>
                            <td>${insertBook.translator}</td>
                        </tr>
                        <tr>
                            <td style="font-weight: bold; vertical-align: top;">類型</td>
                            <td style="vertical-align: top;">：</td>
                            <td>${insertBook.genreBean.genreName}</td>
                        </tr>
                        <tr>
                            <td style="font-weight: bold; vertical-align: top;">ISBN</td>
                            <td style="vertical-align: top;">：</td>
                            <td>${insertBook.isbn}</td>
                        </tr>
                        <tr>
                            <td style="font-weight: bold; vertical-align: top;">出版社</td>
                            <td style="vertical-align: top;">：</td>
                            <td>${insertBook.press}</td>
                        </tr>
                        <tr>
                            <td style="font-weight: bold; vertical-align: top;">價格</td>
                            <td style="vertical-align: top;">：</td>
                            <td>${insertBook.price} 元</td>
                        </tr>
                        <tr>
                            <td style="font-weight: bold; vertical-align: top;">庫存</td>
                            <td style="vertical-align: top;">：</td>
                            <td>${insertBook.stock} 本</td>
                        </tr>
                        <tr>
                            <td style="font-weight: bold; vertical-align: top;">簡述</td>
                            <td style="vertical-align: top;">：</td>
                            <td style="word-break: break-all; max-width: 200px;">${insertBook.shortDesc}</td>
                        </tr>
                    </table>
                </div>
            `,
            icon: 'success',
            confirmButtonText: '確認',
            confirmButtonColor: '#dcd5c7',
            width: '450px' 
        });
        
    <%}%>
};

$(document).on('click', '.archive-btn', function() {
    const btn = $(this);
    const bookId = btn.data('bookid');
    const status = btn.data('status');
    const isArchived = (status == 2);
    const actionText = isArchived ? "解封" : "封存";
    Swal.fire({
        title:`確定要\${actionText}此書籍嗎？`,
        text: isArchived ? "解封後書籍將處於下架狀態。" : "封存後將強制下架且無法進行修改",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: isArchived ? '#28a745' : '#d33',
        confirmButtonText: '確定',
        cancelButtonText: '取消'
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                // 動態決定請求路徑
               url: "<%=request.getContextPath()%>" + (isArchived ? "/books/unarchiveBook" : "/books/archiveBook"),
                type: 'POST',
                data: { bookId: bookId },
                success: function(res) {
                    if (res.trim() === "success") {
                        Swal.fire(`${actionText}成功！`, '', 'success').then(() => {
                            location.reload(); 
                        });
                    }
                }
            });
        }
    });
});
</script>
	<script>
const CONFIRM_COLOR = "#d89696"; // 警告色 (柔和土紅)
const CANCEL_COLOR = "#e8e4dc"; // 輔助色 (淺土色)
//DataTable
$(document).ready(function () {
    $('.books-table').DataTable({
    	"columnDefs": [
            {
                "targets": [ 0 ], 
                "orderable": false 
            }
        ],
        "language": {
            "lengthMenu": "顯示 _MENU_ 項",
            "zeroRecords": "沒有符合的結果",
            "info": "第 _START_ 至 _END_ 項",
            "infoEmpty": "顯示第 0 至 0 項結果，共 0 項",
            "infoFiltered": "(從 _MAX_ 項結果中過濾)",
            "search": "搜索:",
            "paginate": {
                "first": "首頁",
                "previous": "上一頁",
                "next": "下一頁",
                "last": "末頁"
            }
        }
    });
});

$(document).ready(function() {
    $(document).on('change', '.on-shelf-toggle', function() {
        const checkbox = $(this);
        const bookId = checkbox.data('bookid');
        const isChecked = checkbox.is(':checked'); 
        const statusLabel = checkbox.closest('.switch-container').find('.status-label');
        const actionText = isChecked ? "上架" : "下架";

        checkbox.prop('checked', !isChecked);

        Swal.fire({
            title:'確認要將此書籍' + actionText + '嗎？',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#a07d58', 
            cancelButtonColor: '#e8e4dc',
            confirmButtonText: '確定',
            cancelButtonText: '取消'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: '<%=request.getContextPath()%>/books/updateOnShelfStatus',
                    type: 'POST',
                    data: { 
                        bookId: bookId, 
                        status: isChecked 
                    },
                    success: function(res) {
                        if (res.trim() === "success") {
                            checkbox.prop('checked', isChecked);
                            statusLabel.text(isChecked ? "上架中" : "下架中");

                        } else {
                            Swal.fire('失敗', '更新狀態失敗，請聯繫管理員', 'error');
                        }
                    },
                    error: function() {
                        Swal.fire('錯誤', '伺服器通訊失敗', 'error');
                    }
                });
            }
        });
    });
});


</script>

	<script>
	
	
	
	// 修改按鈕防呆檢查
	$('.btn-edit').on('click', function(e) {
	    const btn = $(this);
	    const row = btn.closest('tr');
	    
	    const onShelfStatus = row.find('.status-label').text().trim();

	    if (onShelfStatus === '上架中') {
	        e.preventDefault(); 
	        
	        Swal.fire({
	            icon: 'error',
	            title: '無法修改',
	            text: '書籍目前處於「上架中」狀態，請先將其下架後再進行修改。',
	            confirmButtonColor: '#a07d58' // 皮革棕
	        });
	    }
	});
</script>



</body>
</html>