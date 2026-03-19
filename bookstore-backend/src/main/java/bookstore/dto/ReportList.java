package bookstore.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportList {

    private Integer id;
    private String reporterName;
    private Integer reportedUserId;
    private String reportedName;
    private String bookTitle;
    private String reviewContent;
    private String fullContent;
    private String reason;
    private Integer status;
    private Integer reportedUserRole;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
