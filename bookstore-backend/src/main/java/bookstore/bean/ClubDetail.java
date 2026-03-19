package bookstore.bean;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "club_detail")
@Data
public class ClubDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "clubdetail_id")
	private Integer clubDetailId;

	@OneToOne
	@JoinColumn(name = "club_id")
	@com.fasterxml.jackson.annotation.JsonBackReference
	private BookClubsBean mainClub;

	@Column(name = "purpose")
	private String purpose;

	@Column(name = "diffult_level")
	private Integer diffultLevel;

	@Column(name = "agenda", columnDefinition = "nvarchar(MAX)")
	private String agenda;

	@Column(name = "mode_type")
	private Integer modeType;

	@Column(name = "requirement")
	private String requirement;

	@Column(name = "proof_path")
	private String proofPath;

}
