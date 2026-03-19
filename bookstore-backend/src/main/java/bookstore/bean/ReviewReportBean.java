package bookstore.bean;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "review_reports")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ReviewReportBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_report_id")
	private Integer reviewReportId;

	//	關聯到被檢舉的評論 (Reviews)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_id", insertable = false, updatable = false)
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private ReviewBean review;
	
	//	關聯到檢舉人 (Users)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
	@JsonIgnore
	private UserBean user;
	
	@Column(name = "review_id", nullable = false)
	private Integer reviewId;
	
	@Column(name = "user_id", nullable = false)
	private Integer userId;
	
	@Column(name = "reason", nullable = false)
	private String reason;
	
	@Column(name = "status", nullable = false)
	private String status;
	
	//	時間與處理結果
	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "processed_at")
	private LocalDateTime processedAt;

	@Column(name = "admin_results")
	private String adminResults;
	
	// 建構子初始化預設值 (避免 Java 端 null)
    public ReviewReportBean() {
        this.status = "待處理";
        this.createdAt = LocalDateTime.now();
    }

}
