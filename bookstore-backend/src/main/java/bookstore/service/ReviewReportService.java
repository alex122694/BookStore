package bookstore.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bookstore.bean.ReviewBean;
import bookstore.bean.ReviewReportBean;
import bookstore.dto.ReportList;
import bookstore.repository.ReviewReportRepository;
import bookstore.repository.ReviewRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ReviewReportService {

    @Autowired
    private ReviewReportRepository reportRepo;
    @Autowired
    private ReviewRepository reviewRepo;

    // 查詢單筆
    public ReviewReportBean findById(Integer id) {
        return reportRepo.findById(id).orElse(null);
    }

    // 新增 修改
    public ReviewReportBean save(ReviewReportBean report) {
        return reportRepo.save(report);
    }

    // 檢查是否存在
    public boolean hasReported(Integer userId, Integer reviewId) {
        return reportRepo.existsByUserIdAndReviewId(userId, reviewId);
    }

    // 檢查評論是否可被檢舉
    public boolean isReviewVisible(Integer reviewId) {
        ReviewBean review = reviewRepo.findById(reviewId).orElse(null);
        return review != null && review.getStatus() != null && review.getStatus() == 1;
    }
    
    // 取得待處理的檢舉數量
    public long countPendingReports() {
        return reportRepo.countByStatus("待處理");
    }

    public List<ReportList> getAllReportsForAdmin() {
        // 使用 JOIN FETCH 一次撈取所有關聯資料 (User, Review, Book)
        List<ReviewReportBean> reports = reportRepo.findAllWithDetails();
        List<ReportList> dtoList = new ArrayList<>();

        for (ReviewReportBean report : reports) {

            // 1. 取得檢舉人 (因為有 Fetch，絕不會是 null，除非 DB 資料異常)
            String reporterName = (report.getUser() != null) ? report.getUser().getUserName() : "未知用戶";

            // 2. 取得關聯內容 (書籍、評論、被檢舉人)
            String reportedName = "未知";
            String bookTitle = "未知書籍";
            String fullContent = "評論已刪除";
            String reviewContent = "評論已刪除";
            Integer reportedUserId = null;
            Integer reportedUserRole = null;

            if (report.getReview() != null) {
                ReviewBean review = report.getReview();

                // 評價內容
                fullContent = review.getComment();
                if (fullContent != null) {
                    reviewContent = fullContent.length() > 20 ? fullContent.substring(0, 20) + "..." : fullContent;
                }

                // 書名
                if (review.getBook() != null) {
                    bookTitle = review.getBook().getBookName();
                }

                // 被檢舉人
                if (review.getUser() != null) {
                    reportedName = review.getUser().getUserName();
                    reportedUserId = review.getUser().getUserId();
                    reportedUserRole = review.getUser().getUserType();
                }
            }

            // 3. 轉換狀態碼
            Integer statusInt = 0; // 0:待處理
            String dbStatus = report.getStatus();
            if ("已成立".equals(dbStatus)) {
                statusInt = 1;
            } else if ("已駁回".equals(dbStatus)) {
                statusInt = 2;
            }

            // --- D. 組裝 DTO ---
            ReportList dto = new ReportList(
                    report.getReviewReportId(),
                    reporterName,
                    reportedUserId,
                    reportedName,
                    bookTitle,
                    reviewContent,
                    fullContent,
                    report.getReason(),
                    statusInt,
                    reportedUserRole,
                    report.getCreatedAt());

            dtoList.add(dto);
        }
        return dtoList;
    }

    // 處理審核 (修正重複檢舉會誤開顯示的問題)
    @Transactional
    public void processReport(Integer reportId, Integer newStatus) {

        ReviewReportBean report = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("找不到檢舉紀錄 ID: " + reportId));

        if (newStatus == 1) {
            // 檢舉成立
            report.setStatus("已成立");
            report.setProcessedAt(LocalDateTime.now());
            report.setAdminResults("已下架隱藏");

            // 隱藏評論
            if (report.getReview() != null) {
                ReviewBean review = report.getReview();
                review.setStatus(0);
                reviewRepo.save(review);
            }

        } else if (newStatus == 2) {
            // 檢舉駁回
            report.setStatus("已駁回");
            report.setProcessedAt(LocalDateTime.now());
            report.setAdminResults("檢舉不成立");

        } else {
            report.setStatus("待處理");
        }

        reportRepo.save(report);
    }

}
