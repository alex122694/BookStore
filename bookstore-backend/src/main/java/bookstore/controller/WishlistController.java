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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bookstore.bean.WishlistBean;
import bookstore.service.WishlistService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class WishlistController {
	
	private final WishlistService wishlistService;
	
	@PostMapping("/api/wishlist/toggle")
	public ResponseEntity<?> toggleWish(@RequestBody Map<String, Integer> params) {
		Integer userId = params.get("userId");
		Integer bookId = params.get("bookId");
		
		if (userId == null || bookId == null) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少參數！"));
		}
		
		try {
			boolean isFavorited = wishlistService.toggleWishBooks(userId, bookId);
			Map<String, Object> response = new HashMap<>();
			
			response.put("success", true);
			response.put("isFavorited", isFavorited);
			response.put("message", isFavorited ? "已加入收藏！" : "已從收藏移除");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "系統處理失敗"));
		}
	}
	
	@GetMapping("/api/wishlist/list/{userId}")
	public ResponseEntity<List<WishlistBean>> getWishlist(@PathVariable Integer userId) {
		List<WishlistBean> list = wishlistService.getWishlist(userId);
		return ResponseEntity.ok(list);
	}
	
	@GetMapping("/api/wishlist/check")
	public ResponseEntity<Boolean> checkFavorite(@RequestParam Integer userId, @RequestParam Integer bookId) {
		boolean isFavorited = wishlistService.isFavorited(userId, bookId);
		
		return ResponseEntity.ok(isFavorited);
	}
	
	@DeleteMapping("/api/wishlist/remove/{wishlistId}")
	public ResponseEntity<?> remove(@PathVariable Integer wishlistId) {
		try {
			wishlistService.removeFromWishList(wishlistId);
			return ResponseEntity.ok(Map.of("success", true, "message", "已成功從收藏中移除"));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "移除失敗，請稍後再試"));
		}
	}

}
