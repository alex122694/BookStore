package bookstore.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import bookstore.aop.BusinessException;
import bookstore.bean.BookClubsBean;
import bookstore.bean.ClubConstants;
import bookstore.dto.BookClubRequestDTO;

@SpringBootTest
@Transactional
public class BookClubAdminActionsTest {

    @Autowired
    private BookClubService bookClubService;

    private Integer hostUserId = 1; // Assuming user ID 1 exists as host
    private Integer adminUserId = 100; // Mock admin ID

    private BookClubRequestDTO createPendingClubDTO() {
        BookClubRequestDTO dto = new BookClubRequestDTO();
        dto.setClubName("Admin Action Test Club");
        dto.setEventDate(LocalDateTime.now().plusDays(10));
        dto.setDeadLine(LocalDateTime.now().plusDays(5));
        dto.setPurpose("Test Purpose");
        dto.setAgenda("Test Agenda");
        dto.setLocation("Test Location");
        dto.setAction("SUBMIT"); // Submit directly to PENDING
        return dto;
    }

    @Test
    public void testApproveClub() throws IOException {
        // 1. Create a Pending Club
        BookClubRequestDTO dto = createPendingClubDTO();
        BookClubsBean created = bookClubService.createBookClub(dto, hostUserId);
        assertEquals(ClubConstants.STATUS_PENDING, created.getStatus());

        // 2. Approve
        BookClubsBean approved = bookClubService.approveClub(created.getClubId(), adminUserId);

        // 3. Verify Status
        assertEquals(ClubConstants.STATUS_APPROVED, approved.getStatus());
    }

    @Test
    public void testRejectClub() throws IOException {
        // 1. Create a Pending Club
        BookClubRequestDTO dto = createPendingClubDTO();
        BookClubsBean created = bookClubService.createBookClub(dto, hostUserId);

        // 2. Reject
        String reason = "Violation of terms";
        BookClubsBean rejected = bookClubService.rejectClub(created.getClubId(), reason, adminUserId);

        // 3. Verify Status and Reason
        assertEquals(ClubConstants.STATUS_REJECTED, rejected.getStatus());
        assertEquals(reason, rejected.getRejectionReason());
    }

    @Test
    public void testRejectWithoutReason() throws IOException {
        // 1. Create a Pending Club
        BookClubRequestDTO dto = createPendingClubDTO();
        BookClubsBean created = bookClubService.createBookClub(dto, hostUserId);

        // 2. Expect Exception when rejecting without reason
        assertThrows(BusinessException.class, () -> {
            bookClubService.rejectClub(created.getClubId(), "", adminUserId);
        });
    }
}
