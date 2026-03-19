package bookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import bookstore.bean.ReviewReportBean;

public interface ReviewReportRepository extends JpaRepository<ReviewReportBean, Integer> {

    // [新增] 一次查詢 User, Review, Book, Review User
    @Query("SELECT r FROM ReviewReportBean r " +
            "JOIN FETCH r.user " +
            "JOIN FETCH r.review rev " +
            "JOIN FETCH rev.book " +
            "JOIN FETCH rev.user")
    List<ReviewReportBean> findAllWithDetails();

    // 查詢特定狀態的檢舉 (給後台篩選用)
    // 例如: findByStatus("待處理")
    List<ReviewReportBean> findByStatus(String status);

    // 查詢 User 對某書籍 Review 是否已經檢舉過 (避免重複檢舉)
    boolean existsByUserIdAndReviewId(Integer userId, Integer reviewId);
    
    // [新增] 統計特定狀態的檢舉數量
    long countByStatus(String status);

}
