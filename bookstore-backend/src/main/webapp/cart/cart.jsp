<%@page import="java.util.List" %>
	<%@page import="bookstore.bean.Cart" %>
		<%@page import="bookstore.bean.UserBean" %>
			<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
				<!DOCTYPE html>
				<html>

				<head>
					<meta charset="UTF-8">
					<title>購物車</title>
					<link rel="stylesheet" type="text/css"
						href="https://cdn.datatables.net/2.0.7/css/dataTables.dataTables.min.css">
					<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/bookTable.css">
					<style>
						/* Add specific cart styles */
						.cart-actions {
							display: flex;
							align-items: center;
							justify-content: center;
							gap: 5px;
						}

						.quantity-input {
							width: 50px;
							text-align: center;
						}

						.btn-qty {
							padding: 2px 8px;
							cursor: pointer;
						}

						.total-section {
							margin-top: 20px;
							text-align: right;
							font-size: 1.2em;
							font-weight: bold;
						}

						.checkout-btn {
							background-color: #a07d58;
							color: white;
							padding: 10px 20px;
							border: none;
							border-radius: 4px;
							cursor: pointer;
							font-size: 16px;
							margin-top: 10px;
						}

						.checkout-btn:disabled {
							background-color: #ccc;
							cursor: not-allowed;
						}

						.invalid-item {
							background-color: #ffe6e6;
							/* Light red for invalid */
							color: #d33;
						}

						.error-msg {
							color: red;
							font-size: 0.9em;
							display: block;
						}
					</style>
				</head>

				<body>
					<div id="mainDiv">
						<h2 class="h2-st1">您的購物車</h2>

						<div id="action-bar">
							<form action="<%=request.getContextPath()%>/books/getAllBooks" method="get">
								<input type="submit" value="繼續購物" class="btn-insert" style="background-color: #6c757d;">
							</form>
						</div>

						<table class="books-table" id="cartTable">
							<thead>
								<tr>
									<th style="text-align: center;">商品</th>
									<th style="text-align: center;">單價</th>
									<th style="text-align: center;">數量</th>
									<th style="text-align: center;">小計</th>
									<th style="text-align: center;">操作</th>
								</tr>
							</thead>
							<tbody>
								<!-- Content will be loaded via AJAX -->
								<tr>
									<td colspan="5" style="text-align: center;">載入中...</td>
								</tr>
							</tbody>
						</table>

						<div class="total-section">
							總金額: $<span id="totalAmount">0</span>
							<br>
							<button class="checkout-btn" disabled>前往結帳</button>
							<div id="invalid-msg-div"
								style="color: red; font-size: 0.8em; margin-top: 5px; display:none;">請先移除或調整失效商品</div>
						</div>

					</div>

					<script src="https://code.jquery.com/jquery-3.6.0.js"></script>
					<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

					<script>
						$(document).ready(function () {
							const token = localStorage.getItem('token'); // JWT Token from login

							if (!token) {
								Swal.fire('請先登入', '找不到登入資訊', 'warning').then(() => {
									window.location.href = '<%=request.getContextPath()%>/login';
								});
								return;
							}

							loadCart();

							function loadCart() {
								$.ajax({
									url: '<%=request.getContextPath()%>/cart/api/items',
									type: 'GET',
									headers: { 'Authorization': 'Bearer ' + token },
									dataType: 'json',
									success: function (res) {
										if (res.success) {
											renderCart(res.cartItems, res.totalAmount);
										} else {
											Swal.fire('錯誤', res.message, 'error');
											if (res.message.includes("登入")) {
												// Handle login redirect
											}
										}
									},
									error: function (xhr) {
										console.error(xhr);
										if (xhr.status === 401 || xhr.status === 403) {
											Swal.fire('請先登入', '請重新登入', 'warning').then(() => {
												window.location.href = '<%=request.getContextPath()%>/login';
											});
										} else {
											Swal.fire('錯誤', '無法載入購物車', 'error');
										}
									}
								});
							}

							function renderCart(cartItems, totalAmount) {
								const tbody = $('#cartTable tbody');
								tbody.empty();

								if (!cartItems || cartItems.length === 0) {
									tbody.append('<tr><td colspan="5" style="text-align: center;">購物車是空的</td></tr>');
									$('#totalAmount').text(0);
									$('.checkout-btn').prop('disabled', true);
									$('#invalid-msg-div').hide();
									return;
								}

								let hasInvalidItems = false;

								cartItems.forEach(item => {
									const book = item.booksBean;
									const isShelf = book.onShelf === 1;
									const hasStock = book.stock >= item.quantity;
									const isValid = isShelf && hasStock;
									if (!isValid) hasInvalidItems = true;

									let rowHtml = `
                    <tr class="${!isValid ? 'invalid-item' : ''}" data-cartid="${item.cartId}">
                        <td style="text-align: left;">
                            ${book.bookName}
                            ${!isShelf ? '<span class="error-msg">(已下架)</span>' : ''}
                            ${!hasStock ? `<span class="error-msg">(庫存不足: ${book.stock})</span>` : ''}
                        </td>
                        <td style="text-align: center;" class="price-col">${book.price}</td>
                        <td style="text-align: center;">
                `;

									if (isShelf && book.stock > 0) {
										rowHtml += `
                        <div class="cart-actions">
                            <button type="button" class="btn-qty minus-btn">-</button>
                            <input type="number" class="quantity-input" value="${item.quantity}" 
                                min="1" max="${book.stock}"
                                data-stock="${book.stock}">
                            <button type="button" class="btn-qty plus-btn">+</button>
                        </div>
                    `;
									} else {
										rowHtml += `<span>無法購買</span>`;
									}

									rowHtml += `
                        </td>
                        <td style="text-align: center;" class="subtotal-col">${book.price * item.quantity}</td>
                        <td style="text-align: center;">
                            <button type="button" class="btn btn-danger remove-btn">刪除</button>
                        </td>
                    </tr>
                `;
									tbody.append(rowHtml);
								});

								$('#totalAmount').text(totalAmount);

								if (hasInvalidItems) {
									$('.checkout-btn').prop('disabled', true);
									$('#invalid-msg-div').show();
								} else {
									$('.checkout-btn').prop('disabled', false);
									$('#invalid-msg-div').hide();
								}
							}

							// Update Quantity logic
							function updateQuantity(input, newQty) {
								const row = input.closest('tr');
								const cartId = row.data('cartid');
								const stock = parseInt(input.data('stock'));

								// Basic validation
								if (isNaN(newQty) || newQty < 1) return;
								if (newQty > stock) {
									Swal.fire('庫存不足', '庫存僅剩 ' + stock + ' 本', 'warning');
									input.val(stock);
									newQty = stock;
								}

								// Optimistic UI update or lock? 
								// Locking is safer.
								input.prop('disabled', true);

								$.ajax({
									url: '<%=request.getContextPath()%>/cart/api/update', // API path
									type: 'POST',
									headers: { 'Authorization': 'Bearer ' + token },
									data: { cartId: cartId, quantity: newQty },
									dataType: 'json',
									success: function (res) {
										input.prop('disabled', false);

										if (res.success) {
											input.val(res.quantity);
											row.find('.subtotal-col').text(res.subtotal);
											$('#totalAmount').text(res.totalAmount);
										} else {
											Swal.fire('更新失敗', res.message, 'error');
											input.val(input.data('last-valid-val') || 1);
										}
									},
									error: function () {
										input.prop('disabled', false);
										Swal.fire('錯誤', '連線失敗', 'error');
									}
								});
							}

							// Event delegation for dynamically added elements
							$('#cartTable').on('focus', '.quantity-input', function () {
								$(this).data('last-valid-val', $(this).val());
							});

							$('#cartTable').on('change', '.quantity-input', function () {
								let val = parseInt($(this).val());
								if (val < 1) val = 1;
								updateQuantity($(this), val);
							});

							$('#cartTable').on('click', '.plus-btn', function () {
								const input = $(this).siblings('.quantity-input');
								let val = parseInt(input.val()) || 0;
								updateQuantity(input, val + 1);
							});

							$('#cartTable').on('click', '.minus-btn', function () {
								const input = $(this).siblings('.quantity-input');
								let val = parseInt(input.val()) || 0;
								if (val > 1) {
									updateQuantity(input, val - 1);
								}
							});

							// Remove Item
							$('#cartTable').on('click', '.remove-btn', function () {
								const row = $(this).closest('tr');
								const cartId = row.data('cartid');

								Swal.fire({
									title: '確定移除?',
									icon: 'warning',
									showCancelButton: true,
									confirmButtonText: '移除',
									cancelButtonText: '取消'
								}).then((result) => {
									if (result.isConfirmed) {
										$.ajax({
											url: '<%=request.getContextPath()%>/cart/api/remove', // API path
											type: 'POST',
											headers: { 'Authorization': 'Bearer ' + token },
											data: { cartId: cartId },
											dataType: 'json',
											success: function (res) {
												if (res.success) {
													// Reload whole cart to re-calc invalid states simply
													loadCart();
												} else {
													Swal.fire('失敗', res.message, 'error');
												}
											}
										});
									}
								});
							});

							$('.checkout-btn').click(function () {
								Swal.fire('結帳功能尚未實作', '此為模擬展示', 'info');
							});
						});
					</script>
				</body>

				</html>