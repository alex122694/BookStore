package bookstore.bean;

import java.time.LocalDateTime;

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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="operation_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class UserLogBean {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="logId", nullable = false)
	private Integer logId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "adminId", referencedColumnName = "user_id") 
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "logs"})
	@NonNull
	@ToString.Exclude
	private UserBean adminUser;
	
	@Column(name="adminName") 
	@NonNull
	private String adminName;
	
	@Column(name="action", columnDefinition = "NVARCHAR(MAX)")
	@NonNull
	private String action;
	
	@Column(name="targetId") 
	@NonNull
	private String targetId;
	
	@Column(name="actionTime")
	private LocalDateTime actionTime = LocalDateTime.now();

}