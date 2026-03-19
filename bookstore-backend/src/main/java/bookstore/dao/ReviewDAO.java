package bookstore.dao;

import java.util.List;
import bookstore.bean.ReviewBean;

public interface ReviewDAO {
	public List<ReviewBean> selectAllReviews();
	public ReviewBean selectReviewById(Integer reviewId);
	public int insertReview(ReviewBean review);
	public int updateReview(ReviewBean review);
	public int deleteReview(Integer reviewId);
}
