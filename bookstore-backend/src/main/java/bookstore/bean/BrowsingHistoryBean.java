package bookstore.bean;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "browsing_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrowsingHistoryBean {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer historyId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", referencedColumnName = "user_id")
	@JsonIgnore
	@ToString.Exclude
	private UserBean user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bookId", referencedColumnName = "book_id")
	private BooksBean book;
	
	@Column(name = "browsedAt")
	private Date browsedAt;

}
