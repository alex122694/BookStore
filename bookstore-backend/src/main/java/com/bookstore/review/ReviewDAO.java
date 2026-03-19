package com.bookstore.review;

import java.util.List;
import com.bookstore.review.ReviewBean;

public interface ReviewDAO {
	public List<ReviewBean> selectAllReviews();
	public ReviewBean selectReviewById(Integer reviewId);
	public int insertReview(ReviewBean review);
	public int updateReview(ReviewBean review);
	public int deleteReview(Integer reviewId);
}
