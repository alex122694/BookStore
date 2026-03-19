package bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import bookstore.bean.ReviewBean;

public interface ReviewRepository extends JpaRepository<ReviewBean, Integer> {

	@Query("""
			SELECT r
			FROM ReviewBean r
			JOIN FETCH r.user
			JOIN FETCH r.book
			WHERE r.reviewId = :id
			""")
	Optional<ReviewBean> findByIdWithUserAndBook(@Param("id") Integer id);

	// 統計數量 
	@Query("SELECT COUNT(r) FROM ReviewBean r WHERE r.book.bookId = :bookId AND r.status = 1")
    Integer countByBook_BookId(@Param("bookId") Integer bookId);

	// 計算平均分 
	@Query("SELECT AVG(r.rating) FROM ReviewBean r WHERE r.book.bookId = :bookId AND r.status = 1")
    Double findAvgRatingByBookId(@Param("bookId") Integer bookId);
	
	// 查詢特定會員的所有評價
	@Query("SELECT r FROM ReviewBean r JOIN FETCH r.book JOIN FETCH r.user WHERE r.user.userId = :userId")
    List<ReviewBean> findByUser_UserId(@Param("userId") Integer userId);
}
