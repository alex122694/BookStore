package bookstore.bean;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

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
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "reviews")
@Component
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "user", "book" })
public class ReviewBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id")
	private Integer reviewId;

	// ===== FK → ManyToOne =====

	// ✅ ManyToOne：同一個欄位做關聯，但設為「只讀」，避免重複 mapping
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private UserBean user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private BooksBean book;

	@Column(name = "user_id", nullable = false)
	private Integer userId;

	@Column(name = "book_id", insertable = false, updatable = false)
	private Integer bookId;

	// ===== 其他欄位 =====

	@Column(name = "rating", nullable = false)
	private Integer rating;

	@Column(name = "comment")
	private String comment;

	@Column(name = "created_at", updatable = false)
	@com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createdAt;
	
	@Column(name = "status")
	private Integer status;

	// ====== 過渡期：舊 JSP 可能用到 ======
	@Transient
	private String userName;

	@Transient
	private String bookName;

	@Transient
	private Integer userType;

	// ===== getter / setter =====

	public void setBookId(Integer bookId) {
		if (this.book == null) {
			this.book = new BooksBean();
		}
		this.book.setBookId(bookId);
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	// ✅ 舊 JSP 仍可用：優先從關聯拿名稱（不會再一直 null）
	public String getUserName() {
		return (user != null) ? user.getUserName() : userName;
	}

	public String getBookName() {
		return (book != null) ? book.getBookName() : bookName;
	}

	public Integer getUserType() {
		return userType;
	}

}
