package bookstore.bean;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@Entity
@Table(name = "book_clubs")
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class BookClubsBean {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "club_id")
	private Integer clubId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "host_id", nullable = false)
	private UserBean host;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id")
	private BooksBean book;

	@Column(name = "external_book_info", length = 200)
	private String externalBookInfo;

	@Column(name = "club_name", nullable = false, length = 100)
	private String clubName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private ClubCategoriesBean categoriesBean;

	@Column(name = "event_date")
	private LocalDateTime eventDate;

	@Column(name = "deadline")
	private LocalDateTime deadline;

	@Column(name = "location", length = 200)
	private String location;

	// @Column(name = "proposal_path", nullable = false, length = 500)
	// private String proposalPath;
	//
	// @Column(name = "proof_path", length = 500)
	// private String proofPath;

	@Column(name = "organizer_type")
	private Short organizerType; // TINYINT 對應 Short 或 Integer

	@Column(name = "max_participants")
	private Integer maxParticipants;

	@Column(name = "current_participants")
	private Integer currentParticipants;

	@Column(name = "status")
	private Integer status;

	@Column(name = "rejection_reason", length = 500)
	private String rejectionReason;

	@Lob // 對應 NVARCHAR(MAX)
	@Column(name = "ai_evaluation")
	private String aiEvaluation;

	@OneToOne(mappedBy = "mainClub", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonManagedReference
	private ClubDetail clubDetail;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@jakarta.persistence.Version
	@Column(name = "version")
	private Integer version;
}