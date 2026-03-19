package bookstore.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bookstore.bean.ReviewReportBean;
import bookstore.dto.ReportList;
import bookstore.service.ReviewReportService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewReportController {

    @Autowired
    private ReviewReportService rsReport;

    // 新增檢舉
    @PostMapping("/public/reports")
    public ResponseEntity<?> createReport(@RequestBody ReviewReportBean report) {
        // 基本驗證
        if (report.getReviewId() == null || report.getUserId() == null || report.getReason() == null) {
            return ResponseEntity.badRequest().body("欄位缺漏");
        }

        // 檢查評論是否為「顯示中」(如果不顯示，代表已被隱藏或刪除，則不可檢舉)
        boolean isVisible = rsReport.isReviewVisible(report.getReviewId());
        if (!isVisible) {
            return ResponseEntity.badRequest().body("無法檢舉此評論（評論已隱藏或不存在）");
        }

        // 檢查是否重複檢舉
        boolean exists = rsReport.hasReported(report.getUserId(), report.getReviewId());
        if (exists) {
            return ResponseEntity.status(409).body("您已經檢舉過此評論");
        }

        report.setStatus("待處理");
        report.setCreatedAt(LocalDateTime.now());

        ReviewReportBean saved = rsReport.save(report);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/admin/reports")
    public ResponseEntity<List<ReportList>> getAllReports() {
        List<ReportList> reports = rsReport.getAllReportsForAdmin();
        return ResponseEntity.ok(reports);
    }

    // 2. 更新檢舉狀態 (審核)
    @PutMapping("/admin/reports/{id}")
    public ResponseEntity<?> updateReportStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> payload) {
        Integer newStatus = payload.get("status");

        if (newStatus == null) {
            return ResponseEntity.badRequest().body("狀態不能為空");
        }

        try {
            // 呼叫 Service 更新狀態
            rsReport.processReport(id, newStatus);
            return ResponseEntity.ok().body("{\"message\": \"更新成功\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("更新失敗: " + e.getMessage());
        }
    }
    
    // 取得待處理檢舉數量 (給側邊欄通知)
    @GetMapping("/admin/reports/pending-count")
    public ResponseEntity<?> getPendingReportCount() {
        try {
            long count = rsReport.countPendingReports();
            // 回傳 JSON 格式: { "count": 5 }
            return ResponseEntity.ok().body("{\"count\": " + count + "}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("取得數量失敗");
        }
    }

}
