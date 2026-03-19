package bookstore.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import bookstore.bean.BrowsingHistoryBean;
import bookstore.service.BrowsingHistoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class BrowsingHistoryController {
	
	private final BrowsingHistoryService browsingService;
	
	@PostMapping("/api/history/record")
	public ResponseEntity<?> record(@RequestBody Map<String, Integer> params) {
		Integer userId = params.get("userId");
		Integer bookId = params.get("bookId");
		Map<String, Object> response = new HashMap<>();
		
		if (userId == null || bookId == null) {
			response.put("success", false);
			response.put("message", "缺少必要參數");
			return ResponseEntity.badRequest().body(response);
		}
		
		try {
			browsingService.recordBrowsing(userId, bookId);
			response.put("success", true);
			response.put("message", "紀錄成功！");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "系統處理紀錄失敗");
			return ResponseEntity.internalServerError().body(response);
		}
	}
	
	@GetMapping("api/history/list/{userId}")
	public ResponseEntity<List<BrowsingHistoryBean>> getHistoryList(@PathVariable Integer userId) {
		List<BrowsingHistoryBean> list = browsingService.getHistoryByUser(userId);
		
		return ResponseEntity.ok(list);
	}
	
	@DeleteMapping("/api/history/clear/{userId}")
	public ResponseEntity<?> clearHistory(@PathVariable Integer userId) {
		try {
			browsingService.clearAllHistory(userId);
			return ResponseEntity.ok(Map.of("success", true, "message", "紀錄已全數清空"));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("清空失敗");
		}
	}
}
