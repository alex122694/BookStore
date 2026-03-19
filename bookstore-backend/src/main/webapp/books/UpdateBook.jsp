<%@page import="java.util.List"%>
<%@page import="bookstore.bean.GenreBean"%>
<%@page import="bookstore.bean.BooksBean"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%
BooksBean book = (BooksBean) request.getAttribute("book");
List<GenreBean> genreList = (List<GenreBean>) request.getAttribute("genreList");

if (book == null) {
	response.sendRedirect("BookList"); // 或者其他錯誤處理
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>修改書籍資料 - <%=book.getBookName()%></title>

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
.card {
	width: 90%;
	max-width: 850px; /* 擴大卡片寬度以容納表格 */
	padding: 35px 45px;
	box-sizing: border-box;
	border: 1px solid #dcd5c7;
	border-radius: 6px;
	background-color: #ffffff;
	box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
	text-align: center;
}

/* 標題 */
h2 {
	color: #7b5e47;
	font-size: 26px;
	margin-top: 0;
	margin-bottom: 30px;
	padding-bottom: 15px;
	border-bottom: 1px solid #e0d9c9;
	font-weight: bold;
}

/* ------------------ 表格與欄位樣式 ------------------ */
table {
	width: 100%;
	border-collapse: collapse;
	font-size: 15px;
	margin-bottom: 20px;
}

td {
	padding: 12px 10px;
	text-align: right;
	font-weight: 500;
	color: #6d6d6d;
}

/* 讓輸入欄位靠左對齊 */
td:nth-child(2) {
	text-align: left;
}

/* ------------------ 訊息提示樣式 (與 InsertBook 相同) ------------------ */
.requirement {
	color: red;
	display: block;
	font-size: 13px;
	font-weight: normal;
	height: 18px;
	margin-bottom: 3px;
}

/* 輸入框與下拉式選單共用樣式 */
td input, td select {
	width: 100%;
	padding: 10px 12px;
	font-size: 15px;
	border: 1px solid #d0c8b9;
	border-radius: 4px;
	background: #fefcf9;
	transition: 0.15s;
	box-sizing: border-box;
	height: 40px;
	color: #4a4a4a;
}

td input:focus, td select:focus {
	border-color: #9fb89e; /* 青綠色 focus */
	box-shadow: 0 0 4px rgba(159, 184, 158, .5);
	outline: none;
}

/* 唯讀欄位樣式 */
.read-only {
	background-color: #f7f3e8 !important; /* 淺米色背景 */
	cursor: default;
	color: #7b5e47; /* 棕色文字 */
	font-weight: bold;
	border: 1px solid #dcd5c7;
}

/* ------------------ 按鈕樣式 (主要動作) ------------------ */
.btn-container {
	margin-top: 25px;
	display: flex;
	justify-content: center;
	gap: 15px;
}

.btn {
	padding: 10px 20px;
	font-size: 16px;
	font-weight: bold;
	border: none;
	border-radius: 4px;
	color: white;
	cursor: pointer;
	letter-spacing: 0.1em;
	transition: background-color 0.2s, transform 0.2s, box-shadow 0.2s;
	box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.btn-submit {
	background: #a07d58; /* 皮革棕 */
}

/* hover 效果 */
.btn-submit:hover {
	background: #926f4e;
	box-shadow: 0 6px 15px rgba(0, 0, 0, 0.25);
	transform: translateY(-2px);
}

/* active 效果 */
.btn-submit:active {
	transform: translateY(0);
	box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
}

/* 這裡可以加上一個取消按鈕，使用輔助色 (若不需要，請忽略) */
.btn-back {
	background-color: #e8e4dc;
	color: #4a4a4a;
	font-weight: normal;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.btn-back:hover {
	background-color: #dcd5c7;
	transform: translateY(-1px);
	box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.btn-back:active {
	background-color: #dcd5c7;
	transform: translateY(0);
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>

</head>
<body>

	<div class="card">
		<h2>
			修改書籍資料：<%=book.getBookName()%></h2>
		<form method="post" action="/books/update">
			<table>

				<input type="hidden" name="bookId" id="bookId" value="<%=book.getBookId()%>">

				<tr>
					<td>書籍編號 (ID):</td>
					<td><input type="text" value="<%=book.getBookId()%>" disabled
						class="read-only"></td>
				</tr>

				<tr>
					<td>書籍名稱:</td>
					<td><span id="nameSpan" class="requirement">*</span><input type="text" name="bookName" id="bookName"
						value="<%=book.getBookName()%>" required></td>
				</tr>
				<tr>
					<td>作者:</td>
					<td><span id="authorSpan" class="requirement">*</span><input type="text" name="author" id="author"
						value="<%=book.getAuthor()%>" required></td>
				</tr>
				<tr>
					<td>譯者:</td>
					<td><span id="translatorSpan" class="requirement"></span><input type="text" name="translator" id="translator"
						value="<%=book.getTranslator() == null ? "" : book.getTranslator()%>"></td>
				</tr>
				<tr>
					<td>類型:</td>
					<td><span id="genreSpan" class="requirement">*</span><select name="genre" id="genre" required>
							<option value="" disabled>-- 請選擇書籍類型 --</option>
							<%
							if (genreList != null && !genreList.isEmpty()) {
								for (GenreBean genre : genreList) {
							%>
							<option value="<%=genre.getGenreId()%>"><%=genre.getGenreName()%></option>
							<%
							}
							} else {
							%>
							<option value="" disabled>無可用類型資料</option>
							<%
							}
							%>
					</select></td>
				</tr>
				<tr>
					<td>出版社:</td>
					<td><span id="pressSpan" class="requirement">*</span><input type="text" name="press" id="press"
						value="<%=book.getPress()%>" required></td>
				</tr>
				<tr>
					<td>價錢:</td>
					<td><span id="priceSpan" class="requirement">*</span><input type="text" name="price" id="price"
						value="<%=book.getPrice()%>" required></td>
				</tr>
				<tr>
					<td>ISBN:</td>
					<td><span id="isbnSpan" class="requirement">*</span><input type="text" name="isbn" id="isbn" value="<%=book.getIsbn()%>"
						maxlength="13" required></td>
				</tr>
				<tr>
					<td>庫存:</td>
					<td><span id="stockSpan" class="requirement">*</span><input type="text" name="stock" id="stock"
						value="<%=book.getStock()%>" required></td>
				</tr>
				<tr>
					<td>簡述:</td>
					<td><span id="shortDescSpan" class="requirement"></span><input type="text" name="short_desc" id="shortDesc"
						value="<%=book.getShortDesc() == null ? "" : book.getShortDesc()%>"></td>
				</tr>
				<!-- 
				<tr>
					<td>上下架狀態:</td>
								(True)</option>
								(False)</option>
					</select></td>
				</tr>
				 -->
			</table>
			<div class="btn-container">
				<input type="submit" class="btn btn-submit" value="確認修改">
				<button type="submit" class="btn btn-back"
				method="get" action="/books/getAllBooks">返回</button>
			</div>
		</form>
	</div>

	<script src="https://code.jquery.com/jquery-3.6.0.js"></script>
	<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

	<script>
		$(function() {
			const originalIsbn = $('#isbn').val();

			// 書名檢查
			$('#bookName').on('input', function() {
				let content = $(this).val().trim();
				$('#nameSpan').text(content.length === 0 ? '書名不可為空白！' : '✓').css('color', content.length === 0 ? 'red' : 'green');
			});

			// 作者檢查
			$('#author').on('input', function() {
				let content = $(this).val().trim();
				$('#authorSpan').text(content.length === 0 ? '作者不可為空白' : '✓').css('color', content.length === 0 ? 'red' : 'green');
			});

			// ISBN 檢查 (含 Ajax)
			$('#isbn').on('input', function() {
				let content = $(this).val().trim();
				let $span = $('#isbnSpan');
				let rule = /^\d{13}$/;

				if (content === originalIsbn) {
					$span.text('✓ (未變更)').css('color', 'green');
					return;
				}

				if (content.length === 0) {
					$span.text('ISBN不可空白').css('color', 'red');
				} else if (!rule.test(content)) {
					$span.text('ISBN必須為13碼數字').css('color', 'red');
				} else {
					$.ajax({
						url: '<%=request.getContextPath()%>/isbnCheck',
						type: 'post',
						data: { isbn: content },
						success: function(res) {
							if (res.trim() === "false") {
								$span.text('✓').css('color', 'green');
							} else {
								$span.text('此ISBN已存在').css('color', 'red');
							}
						}
					});
				}
			});

			// 價格檢查
			$('#price').on('input', function() {
				let content = $(this).val();
				let rule = /^[0-9]+(\.[0-9]{1,2})?$/;
				if (content.length === 0) {
					$('#priceSpan').text('請輸入價格').css('color', 'red');
				} else if (!rule.test(content)) {
					$('#priceSpan').text('只能輸入數字').css('color', 'red');
				} else {
					$('#priceSpan').text('✓').css('color', 'green');
				}
			});

			// 出版社檢查
			$('#press').on('input', function() {
				let content = $(this).val().trim();
				$('#pressSpan').text(content.length === 0 ? '出版社不可空白' : '✓').css('color', content.length === 0 ? 'red' : 'green');
			});

			// 庫存檢查
			$('#stock').on('input', function() {
				let content = $(this).val();
				let rule = /^[0-9]+$/;
				if (content.length === 0) {
					$('#stockSpan').text('請輸入庫存').css('color', 'red');
				} else if (!rule.test(content)) {
					$('#stockSpan').text('只能輸入數字').css('color', 'red');
				} else {
					$('#stockSpan').text('✓').css('color', 'green');
				}
			});

			// 類型檢查
			$('#genre').on('change', function() {
				$('#genreSpan').text('✓').css('color', 'green');
			});

			// 表單送出檢查
			$('.btn-submit').on('click', function(e) {
				let isAllOk = true;
				// 手動觸發一次所有必填項驗證
				$('#bookName, #author, #press, #price, #isbn, #stock').trigger('input');
				
				$('.requirement').each(function() {
					let t = $(this).text();
					if (t !== '✓' && t !== '✓ (未變更)' && t !== '*' && t !== '') {
						isAllOk = false;
					}
				});

				if (!isAllOk) {
					e.preventDefault();
					Swal.fire("提醒", "請確認標記為紅色的欄位內容是否正確。", "error");
				}
			});

			// 初始化：進入頁面先標示原本正確的欄位
			$('#bookName, #author, #press, #price, #isbn, #stock').trigger('input');
			if($('#genre').val()) $('#genreSpan').text('✓').css('color', 'green');
		});
	</script>
</body>
</html>