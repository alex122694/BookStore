package bookstore.bean;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name= "chat_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageBean {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer msgId;
	
	private Integer senderId;
	
	private Integer receiverId;
	
	private String content;
	
	@Builder.Default
	@JsonProperty("isRead")
	@Column(name = "isRead")
	private Boolean isRead = false;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Builder.Default
	private Date createdAt = new Date();
	
}
