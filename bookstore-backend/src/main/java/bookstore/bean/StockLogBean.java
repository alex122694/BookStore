package bookstore.bean;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockLogBean {

	@Id
	@Column(name = "log_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer logId;
	
	@Column(name = "wholesaler")
	private String wholesaler;
	
	@Column(name = "log_time")
	private Date logTime;

	@Column(name = "total_amount")
	private BigDecimal totalAmount;
	
	@Column(name = "stock_type")
	private Integer stockType;
	
	@OneToMany(mappedBy = "stockLogBean",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
	private List<LogItemBean> logItemBeans;
}
