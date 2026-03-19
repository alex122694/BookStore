package bookstore.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bookstore.aop.BusinessException;
import bookstore.bean.BookClubsBean;
import bookstore.bean.ClubConstants;
import bookstore.bean.ClubDetail;
import bookstore.bean.UserBean;
import bookstore.dto.BookClubRequestDTO;
import bookstore.repository.BookClubsRepository;
import bookstore.repository.BookRepository;
import bookstore.repository.ClubCategoriesRepository;
import bookstore.repository.ClubDetailRepository;
import bookstore.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class BookClubFlowSimulationTest {

    @InjectMocks
    private BookClubService bookClubService;

    @Mock
    private BookClubsRepository bookClubsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClubDetailRepository clubDetailRepository;

    @Mock
    private ClubCategoriesRepository categoriesRepository;

    @Mock
    private BookRepository bookRepository;

    private UserBean mockUser;
    private BookClubRequestDTO validDto;

    @BeforeEach
    void setUp() {
        mockUser = new UserBean();
        mockUser.setUserId(1);
        mockUser.setUserName("TestUser");
        mockUser.setPoints(100);

        validDto = new BookClubRequestDTO();
        validDto.setClubName("Simulation Club");
        validDto.setEventDate(LocalDateTime.now().plusDays(5));
        validDto.setDeadLine(LocalDateTime.now().plusDays(2));
        validDto.setLocation("Online");
        validDto.setPurpose("Study");
        validDto.setAgenda("Read");
        validDto.setModeType(1);
        validDto.setRequirement("None");
        validDto.setMaxParticipants(10);
    }

    /**
     * 流程模擬 1: 填寫資料 -> 儲存草稿
     */
    @Test
    void testScenario_CreateDraft() throws IOException {
        System.out.println("模擬流程 1: 建立草稿 (DRAFT)");
        validDto.setAction("DRAFT");

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(bookClubsRepository.save(any(BookClubsBean.class))).thenAnswer(i -> {
            BookClubsBean b = (BookClubsBean) i.getArguments()[0];
            b.setClubId(101);
            return b;
        });

        BookClubsBean result = bookClubService.createBookClub(validDto, 1);

        assertNotNull(result);
        assertEquals(ClubConstants.STATUS_DRAFT, result.getStatus(), "狀態應為 DRAFT");
        System.out.println("-> 成功建立草稿，ID: " + result.getClubId());
    }

    /**
     * 流程模擬 2: 填寫資料 -> 直接送審
     */
    @Test
    void testScenario_CreateAndSubmit() throws IOException {
        System.out.println("模擬流程 2: 直接送審 (SUBMIT)");
        validDto.setAction("SUBMIT");

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(bookClubsRepository.save(any(BookClubsBean.class))).thenAnswer(i -> {
            BookClubsBean b = (BookClubsBean) i.getArguments()[0];
            b.setClubId(102);
            return b;
        });

        BookClubsBean result = bookClubService.createBookClub(validDto, 1);

        assertNotNull(result);
        assertEquals(ClubConstants.STATUS_PENDING, result.getStatus(), "狀態應為 PENDING (審核中)");
        System.out.println("-> 成功送審，ID: " + result.getClubId());
    }

    /**
     * 流程模擬 3: 草稿 -> 編輯 -> 送審
     */
    @Test
    void testScenario_UpdateDraftToSubmit() throws IOException {
        System.out.println("模擬流程 3: 草稿 -> 編輯 -> 送審");

        // 1. 準備現有草稿
        BookClubsBean existingClub = new BookClubsBean();
        existingClub.setClubId(101);
        existingClub.setHost(mockUser);
        existingClub.setStatus(ClubConstants.STATUS_DRAFT);
        existingClub.setClubName("Old Draft Name");
        existingClub.setMaxParticipants(5);
        existingClub.setEventDate(LocalDateTime.now().plusDays(5));
        existingClub.setDeadline(LocalDateTime.now().plusDays(2)); // 注意: 原有代碼拼字 Deadline vs DeadLine

        ClubDetail existingDetail = new ClubDetail();
        existingDetail.setMainClub(existingClub);

        when(bookClubsRepository.findById(101)).thenReturn(Optional.of(existingClub));
        when(clubDetailRepository.findByMainClub_ClubId(101)).thenReturn(Optional.of(existingDetail));
        when(bookClubsRepository.save(any(BookClubsBean.class))).thenReturn(existingClub);

        // 2. 更新請求 (送審)
        validDto.setAction("SUBMIT");
        validDto.setClubName("New Club Name");

        BookClubsBean updated = bookClubService.updateBookclub(101, validDto, 1, ClubConstants.ROLE_MEMBER);

        assertEquals("New Club Name", updated.getClubName());
        assertEquals(ClubConstants.STATUS_PENDING, updated.getStatus(), "狀態應轉為 PENDING");
        System.out.println("-> 草稿編輯並送審成功");
    }

    /**
     * 流程模擬 4: 審核中 -> 會員嘗試修改 (應失敗)
     */
    @Test
    void testScenario_UpdatePending_ShouldFail() {
        System.out.println("模擬流程 4: 審核中 -> 會員嘗試修改 (預期失敗)");

        BookClubsBean existingClub = new BookClubsBean();
        existingClub.setClubId(103);
        existingClub.setHost(mockUser);
        existingClub.setStatus(ClubConstants.STATUS_PENDING);

        when(bookClubsRepository.findById(103)).thenReturn(Optional.of(existingClub));

        validDto.setAction("SUBMIT");

        assertThrows(BusinessException.class, () -> {
            bookClubService.updateBookclub(103, validDto, 1, ClubConstants.ROLE_MEMBER);
        });
        System.out.println("-> 成功阻擋審核中修改");
    }

    /**
     * 流程模擬 5: 管理員駁回 -> 會員修改重送
     */
    @Test
    void testScenario_RejectedToPending() throws IOException {
        System.out.println("模擬流程 5: 駁回 -> 修改 -> 重送");

        // 1. 準備已駁回的活動
        BookClubsBean existingClub = new BookClubsBean();
        existingClub.setClubId(104);
        existingClub.setHost(mockUser);
        existingClub.setStatus(ClubConstants.STATUS_REJECTED);
        existingClub.setRejectionReason("資料不全");
        existingClub.setClubName("Rejected Club");
        existingClub.setMaxParticipants(5);
        existingClub.setEventDate(LocalDateTime.now().plusDays(5));
        existingClub.setDeadline(LocalDateTime.now().plusDays(2));

        ClubDetail existingDetail = new ClubDetail();
        existingDetail.setMainClub(existingClub);

        when(bookClubsRepository.findById(104)).thenReturn(Optional.of(existingClub));
        when(clubDetailRepository.findByMainClub_ClubId(104)).thenReturn(Optional.of(existingDetail));
        when(bookClubsRepository.save(any(BookClubsBean.class))).thenReturn(existingClub);

        // 2. 會員修正並重送
        validDto.setAction("SUBMIT");
        validDto.setClubName("Fixed Club");

        BookClubsBean updated = bookClubService.updateBookclub(104, validDto, 1, ClubConstants.ROLE_MEMBER);

        assertEquals(ClubConstants.STATUS_PENDING, updated.getStatus(), "狀態應轉回 PENDING");
        assertNull(updated.getRejectionReason(), "駁回原因應清空");
        System.out.println("-> 駁回後修正並重送成功");
    }
}
