package bookstore.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bookstore.bean.BooksBean;
import bookstore.bean.ReviewBean;
import bookstore.dto.ReviewList;
import bookstore.repository.BookRepository;
import bookstore.repository.ReviewRepository;

@Service
@Transactional
public class ReviewsService {
	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private BookRepository bookRepository;

	// 查詢全部
	public List<ReviewBean> findAllReviews() {

		List<ReviewBean> reviews = reviewRepository.findAll();

		for (ReviewBean review : reviews) {
			if (review.getUser() != null) {
				review.setUserType(review.getUser().getUserType());
			}
		}

		return reviews;
	}

	// 查詢單筆（含 user、book）
	public ReviewBean findById(Integer reviewId) {

		ReviewBean review = reviewRepository.findByIdWithUserAndBook(reviewId).orElse(null);

		if (review != null && review.getUser() != null) {
			review.setUserType(review.getUser().getUserType());
		}

		return review;
	}

	// 新增 / 更新（JPA save 兩用）
	public ReviewBean save(ReviewBean review) {
		if (review.getCreatedAt() == null) {
			review.setCreatedAt(LocalDateTime.now());
		}

		// 如果是新增 (ID 為 null) 且沒有特別指定狀態，預設設為 1 (正常顯示)
		if (review.getReviewId() == null && review.getStatus() == null) {
			review.setStatus(1);
		}

		ReviewBean savedReview = reviewRepository.save(review);

		if (savedReview.getUser() != null) {
			savedReview.setUserType(savedReview.getUser().getUserType());
		}

		return savedReview;
	}

	// 刪除
	public void delete(Integer reviewId) {
		reviewRepository.deleteById(reviewId);
	}

	// 取得書籍列表並包含評價數據
	public List<ReviewList> findAllBooksWithStats() {
		// 1. 借用 BookRepository 撈出所有書籍
		List<BooksBean> allBooks = bookRepository.findAll();

		// 2. 準備回傳的 List
		List<ReviewList> resultList = new java.util.ArrayList<>();

		// 3. 跑迴圈組裝資料
		for (BooksBean book : allBooks) {
			ReviewList dto = new ReviewList();

			// 搬運書籍基本資料
			dto.setBookId(book.getBookId());
			dto.setBookName(book.getBookName());
			dto.setAuthor(book.getAuthor());
			dto.setPress(book.getPress());

			// 4. 查詢評價數據 (呼叫 ReviewRepository)
			Integer count = reviewRepository.countByBook_BookId(book.getBookId());
			Double avg = reviewRepository.findAvgRatingByBookId(book.getBookId());

			// 5. 填入數據
			dto.setReviewCount(count != null ? count : 0);
			dto.setAvgRating(avg != null ? avg : 0.0);

			// 6. 加入結果列表
			resultList.add(dto);
		}

		return resultList;
	}
	
	// 查詢某使用者的所有評價
    public List<ReviewBean> findReviewsByUserId(Integer userId) {
        List<ReviewBean> reviews = reviewRepository.findByUser_UserId(userId);
        // 為了避免前端讀取 user 物件轉 JSON 出錯或需要額外資訊，可以在這裡補上 userType 等邏輯
        for (ReviewBean review : reviews) {
            if (review.getUser() != null) {
                review.setUserType(review.getUser().getUserType());
            }
        }
        return reviews;
    }
}
