package bookstore.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import bookstore.bean.StockLogBean;
import bookstore.service.OrderService;
import bookstore.service.StockLogService;

@RestController
@RequestMapping("/api/log")
public class StockLogController {

	@Autowired
	private StockLogService stockLogService;

	@Autowired
	private OrderService orderService;

	@GetMapping("/getAllStockLogs")
	public List<StockLogBean> getAllStockLogs() {
		List<StockLogBean> stockLogBeans = stockLogService.getAllStockLogs();
		return stockLogBeans;
	}

	@GetMapping("/getStockLog")
	public ResponseEntity<StockLogBean> getStockLog(Integer logid) {
		StockLogBean result = stockLogService.getStock(logid);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/insert")
	public ResponseEntity<StockLogBean> insertStockLog(@RequestBody StockLogBean stockLogBean) {
		StockLogBean result = stockLogService.insertStockLog(stockLogBean);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/return")
	public ResponseEntity<StockLogBean> returnStockLog(@RequestBody StockLogBean stockLogBean) {
		StockLogBean result = stockLogService.returnStockLog(stockLogBean);
		return ResponseEntity.ok(result);
	}
	@ResponseBody
	@DeleteMapping("/delete/{logId}")
	public ResponseEntity<String> deleteStockLog(@PathVariable Integer logId){
		stockLogService.deleteStockLog(logId);
		return ResponseEntity.noContent().build();
	}

	@ResponseBody
	@PostMapping("/paidtotalSales")
	public BigDecimal paidtotalSals() {
		return stockLogService.paidTotalSales();
	}

	@ResponseBody
	@PostMapping("/unpaidtotalSales")
	public BigDecimal unpaidTotalSales() {
		return stockLogService.unpaidtotalSales();
	}
}
