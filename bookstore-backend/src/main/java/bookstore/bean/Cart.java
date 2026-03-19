package bookstore.bean;

import java.io.Serializable;
import java.sql.Timestamp;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carts")
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })//避免在 JSON 轉換時，因為 Hibernate 的延遲加載（Lazy Loading）產生循環引用或代理物件報錯
public class Cart implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cart_id")
	private Integer cartId;

	@Column(name = "user_id") // 保留基本欄位以便存取，或依賴關聯
	private Integer userId;

	@Column(name = "book_id") // 保留基本欄位以便存取
	private Integer bookId;

	@Column(name = "quantity")
	private Integer quantity;

	@Column(name = "created_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Timestamp createdAt;

	@Column(name = "updated_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Timestamp updatedAt;

	@Column(name = "cart_status")
	private String cartStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
	private UserBean userBean;

	@ManyToOne(fetch = FetchType.EAGER) // 急切載入，可更快顯示購物車裡的書籍資料
	@JoinColumn(name = "book_id", insertable = false, updatable = false)
	private BooksBean booksBean;

	public Cart(Integer userId, Integer bookId, Integer quantity) {
		this.userId = userId;
		this.bookId = bookId;
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "Cart [cartId=" + cartId + ", userId=" + userId + ", bookId=" + bookId + ", quantity=" + quantity
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", cartStatus=" + cartStatus + "]";
	}
}
