package bookstore.bean;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "log_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogItemBean {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "log_item_id")
	private Integer logItemId;

	@Column(name = "change_qty")
	private Integer changeQty;

	@Column(name = "cost_price")
	private BigDecimal costPrice;

	@ManyToOne
	@JoinColumn(name = "log_id")
	@JsonIgnore
	private StockLogBean stockLogBean;

	@ManyToOne
	@JoinColumn(name = "book_id")
	private BooksBean booksBean;
}
