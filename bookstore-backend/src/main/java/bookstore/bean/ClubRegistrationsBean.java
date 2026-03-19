package bookstore.bean;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@Table(name = "club_registrations")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ClubRegistrationsBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reg_id")
    private Integer regId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private BookClubsBean bookClub; // 關聯讀書會

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserBean user; // 關聯報名者

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "check_in")
    private Boolean checkIn; // BIT 對應 Boolean

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;
}