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
@Table(name = "wishlist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistBean {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer wishlistId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId")
	@JsonIgnore
	@ToString.Exclude
	private UserBean user;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "bookId")
	private BooksBean book;
	
	@Column(name = "createdAt")
	private Date createdAt;

}
