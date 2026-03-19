package bookstore.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bookstore.bean.ReviewBean;
import bookstore.dto.ReviewList;
import bookstore.service.ReviewsService;

@RestController
@RequestMapping("/api/public/admin/reviews")
public class ReviewController {

	@Autowired
	private ReviewsService reviewsService;

	// 【取得所有評論】
	@GetMapping
	public List<ReviewBean> getAllReviewsForVue() {
		return reviewsService.findAllReviews();
	}

	// 【取得單筆評論】
	@GetMapping("/{id}")
	public ReviewBean getReviewForVue(@PathVariable Integer id) {
		return reviewsService.findById(id);
	}

	// 【新增評論】
	@PostMapping
	public ReviewBean insertReviewForVue(@RequestBody ReviewBean review) {
		
		review.setCreatedAt(LocalDateTime.now());
		review.setStatus(1);
		reviewsService.save(review);
		
		return review;
	}

	// 【Vue - 修改評論（JSON）】
	@PutMapping("/{id}")
	public ReviewBean updateReviewForVue(@PathVariable Integer id, @RequestBody ReviewBean review) {

		ReviewBean dbReview = reviewsService.findById(id);

		if (dbReview == null) {
			throw new RuntimeException("找不到評價" + id);
		}

		dbReview.setRating(review.getRating());
		dbReview.setComment(review.getComment());

		if (review.getStatus() != null) {
			dbReview.setStatus(review.getStatus());
		}

		reviewsService.save(dbReview);

		return dbReview;
	}

	// 【刪除評論】
	@DeleteMapping("/{id}")
	public void deleteReviewForVue(@PathVariable Integer id) {
		reviewsService.delete(id);
	}

	// 【Vue - 取得書籍列表與評價統計 】
	@GetMapping("/books-stats")
	public List<ReviewList> getBooksWithReviewStats() {
		return reviewsService.findAllBooksWithStats();
	}
	
	// 取得指定 User 的所有評價
    @GetMapping("/user/{userId}")
    public List<ReviewBean> getUserReviews(@PathVariable Integer userId) {
        return reviewsService.findReviewsByUserId(userId);
    }

}
