package bookstore.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BookClubRequestDTO {

	// 控制欄位
	// "DRAFT"(儲存草稿) OR "Submit" (送審/填寫完成)
	private String action;

	// 主表欄位-----BookClub
	private String clubName;
	private Integer categoryId;
	private Integer bookId;
	private Integer maxParticipants;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime eventDate;

	@JsonProperty("deadline")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime deadLine;
	private String location;

	// 附表欄位-----ClubDetail
	private String purpose;
	private String agenda;
	private Integer modeType;
	private String requirement;

	// 新增欄位：佐證資料路徑
	private String proofPath;

	// 難度等級：1:入門, 2:進階, 3:專家
	private Integer difficulty;
}
