package bookstore.service;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bookstore.aop.BusinessException;
import bookstore.bean.BooksBean;
import bookstore.bean.LogItemBean;
import bookstore.bean.OrderItem;
import bookstore.bean.Orders;
import bookstore.bean.StockLogBean;
import bookstore.repository.BookRepository;
import bookstore.repository.OrderItemRepository;
import bookstore.repository.OrdersRepository;
import bookstore.repository.StockLogRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockLogService {
	@Autowired
	private StockLogRepository stockLogRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private OrdersRepository ordersRepository;

	private static final String STATUS_PAID = "已付款";
	private static final String STATUS_UNPAID = "未付款";

	@Transactional
	public List<StockLogBean> getAllStockLogs() {
		List<StockLogBean> stockLogList = stockLogRepository.findAll();
		if (stockLogList.isEmpty()) {
			log.warn("查無任何貨單");
			return stockLogList;
		}
		log.info("查詢貨單成功，取得 {} 筆資料", stockLogList.size());
		return stockLogList;
	}

	@Transactional
	public StockLogBean getStock(Integer logId) {
		if (logId == null) {
			log.warn("查詢失敗 - 貨單ID不可為空白");
			throw new BusinessException(400, "貨單ID不可為空白");
		}
		Optional<StockLogBean> opt = stockLogRepository.findById(logId);
		if (opt.isPresent()) {
			log.info("查詢成功 - 取得ID:{} 貨單資料", opt.get().getLogId());
			return opt.get();
		}
		throw new BusinessException(404, "查無ID" + logId + "相關資料");
	}

	@Transactional
	public void deleteStockLog(Integer logId) {
		Optional<StockLogBean> opt = stockLogRepository.findById(logId);
		if (opt.isEmpty()) {
			log.warn("查無ID:{}貨單資料", logId);
			throw new BusinessException(404, "查無ID:" + logId + "貨單資料");
		}
		stockLogRepository.deleteById(logId);

	}

	@Transactional
	public StockLogBean insertStockLog(StockLogBean stockLogBean) {
		// 1. 設定日期
		if (stockLogBean.getLogTime() == null) {
			stockLogBean.setLogTime(new java.sql.Date(System.currentTimeMillis()));
		}

		BigDecimal totalAmount = BigDecimal.ZERO;
		List<LogItemBean> items = stockLogBean.getLogItemBeans();

		if (items != null) {
			for (LogItemBean item : items) {
				// 建立關聯
				item.setStockLogBean(stockLogBean);

				// 計算金額 (成本 * 數量)
				Integer qty = item.getChangeQty() != null ? item.getChangeQty() : 0;
				BigDecimal subTotal = item.getCostPrice().multiply(BigDecimal.valueOf(qty));
				totalAmount = totalAmount.add(subTotal);

				// 處理庫存
				if (item.getBooksBean() != null && item.getBooksBean().getBookId() != null) {
					Integer bookId = item.getBooksBean().getBookId();
					// 使用悲觀鎖避免併發問題
					Optional<BooksBean> bookOpt = bookRepository.findByIdWithLock(bookId);

					if (bookOpt.isPresent()) {
						BooksBean book = bookOpt.get();
						item.setBooksBean(book); // 確保關聯到完整的 Entity

						Integer currentStock = book.getStock();
						Integer changeQty = item.getChangeQty() != null ? item.getChangeQty() : 0;
						Integer newStock = currentStock;

						// stockType 1: 進貨 (加), 2: 退貨 (減)
						if (stockLogBean.getStockType() == 1) {
							newStock = currentStock + changeQty;
						} else if (stockLogBean.getStockType() == 2) {
							newStock = currentStock - changeQty;
							if (newStock < 0) {
								log.warn("庫存不足，無法退貨");
								throw new BusinessException(400, "書籍 " + book.getBookName() + " 庫存不足，無法退貨");
							}
						}

						book.setStock(newStock);
						bookRepository.save(book);
					} else {
						log.warn("查無ID:{}相關書籍資料", bookId);
						throw new BusinessException(404, "查無書籍ID: " + bookId);
					}
				}
			}
		}

		stockLogBean.setTotalAmount(totalAmount);
		return stockLogRepository.save(stockLogBean);
	}

	// 取得已付款營業額
	public BigDecimal paidTotalSales() {
		BigDecimal totalSales = BigDecimal.ZERO;

		List<Orders> ordersList = ordersRepository.findByPaymentStatus(STATUS_PAID);
		for (Orders orders : ordersList) {
			totalSales.add(orders.getTotalAmount());
		}
		return totalSales;
	}

	@Transactional
	public StockLogBean returnStockLog(StockLogBean inputBean) {
		Integer logId = inputBean.getLogId();
		if (logId == null) {
			log.warn("貨單ID不可空白");
			throw new BusinessException(400, "貨單ID不可為空白");
		}

		// 1. 查詢原單據
		Optional<StockLogBean> opt = stockLogRepository.findById(logId);
		if (!opt.isPresent()) {
			log.warn("查無ID:{}貨單相關資料", logId);
			throw new BusinessException(404, "查無此貨單 ID:" + logId);
		}

		StockLogBean logBean = opt.get();

		// 2. 驗證是否已經是退貨單
		if (logBean.getStockType() == 2) {
			throw new BusinessException(400, "此貨單已經是退貨單，不可重複退貨");
		}

		// 3. 處理庫存回沖 (原本是進貨+，現在要變成退貨-，所以要減去原數量)
		List<LogItemBean> items = logBean.getLogItemBeans();
		if (items != null) {
			for (LogItemBean item : items) {
				if (item.getBooksBean() != null) {
					BooksBean book = item.getBooksBean();
					// 重新查詢書籍以確保庫存最新 (使用悲觀鎖)
					Optional<BooksBean> bookOpt = bookRepository.findByIdWithLock(book.getBookId());
					if (bookOpt.isPresent()) {
						book = bookOpt.get();
						Integer currentStock = book.getStock();
						Integer qtyToRevert = item.getChangeQty() != null ? item.getChangeQty() : 0;

						// 檢查庫存是否足夠扣除
						if (currentStock < qtyToRevert) {
							log.warn("書籍:{} 目前庫存不足以執行退貨");
							throw new BusinessException(400, "書籍 [" + book.getBookName() + "] 目前庫存(" + currentStock
									+ ")不足以執行退貨(" + qtyToRevert + ")");
						}

						// 扣除庫存
						book.setStock(currentStock - qtyToRevert);
						bookRepository.save(book);
					}
				}
			}
		}

		// 4. 更新單據狀態為退貨 (StockType = 2)
		logBean.setStockType(2); // 2: 退貨

		return stockLogRepository.save(logBean);
	}

	// 取得未付款營業額
	public BigDecimal unpaidtotalSales() {
		BigDecimal totalSales = BigDecimal.ZERO;
		List<Orders> ordersList = ordersRepository.findByPaymentStatus(STATUS_UNPAID);
		for (Orders orders : ordersList) {
			totalSales.add(orders.getTotalAmount());
		}
		return totalSales;
	}
}
