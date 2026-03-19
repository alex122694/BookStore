package bookstore.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "users")
@DynamicInsert
@DynamicUpdate
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reviews", "orders"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBean implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "user_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "user_pwd")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String userPwd;
	
	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "gender")
	private String gender;
	
	@Column(name = "birth")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date birth;
	
	@Column(name = "phone_num")
	private String phoneNum;
	
	@Column(name = "address")
	private String address;
	
	@Column(name = "img", length = 255) 
	private String img;
	
	@Column(name = "points")
	private Integer points;
	
	@Column(name = "user_type")
	private Integer userType;
	
	@Column(name = "status")
	private Integer status;
	
	@Column(name = "created_by")
	private String createdBy;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private Date createdAt;
	
	@Column(name = "updated_by")
	private String updatedBy;
	
	@Column(name = "updated_at", insertable = false, updatable = false)
	private Date updatedAt;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	@JsonIgnore
	@Builder.Default
	@ToString.Exclude
	private List<ReviewBean> reviews = new ArrayList<>();
//
	@OneToMany(mappedBy = "userBean", fetch = FetchType.LAZY)
	@JsonIgnore
	@Builder.Default
	@ToString.Exclude
	private List<Orders> orders = new ArrayList<>();
	
	@OneToMany(mappedBy = "adminUser", cascade = CascadeType.ALL)
	@JsonIgnore
	@ToString.Exclude
	private List<UserLogBean> logs;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonIgnore
	@ToString.Exclude
	private List<BrowsingHistoryBean> browsingHistories;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonIgnore
	@ToString.Exclude
	private List<WishlistBean> wishlists = new ArrayList<>();

//	@OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
//	private List<Cart> carts = new ArrayList<>();

}
