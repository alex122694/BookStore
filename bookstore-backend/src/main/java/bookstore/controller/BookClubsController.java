package bookstore.controller;

import java.io.IOException;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import bookstore.aop.BusinessException;
import bookstore.bean.BookClubsBean;
import bookstore.bean.ClubCategoriesBean;
import bookstore.bean.ClubConstants;
import bookstore.bean.UserBean;
import bookstore.dto.BookClubRequestDTO;
import bookstore.service.BookClubService;
import bookstore.service.EmailService;
import bookstore.service.UsersService;
import bookstore.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/clubs")
@DynamicInsert
public class BookClubsController {

	private final EmailService emailService;

	@Autowired
	private BookClubService bookClubService;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private UsersService usersService;

	BookClubsController(EmailService emailService) {
		this.emailService = emailService;
	}

	@GetMapping("/allClubs")
	public ResponseEntity<List<BookClubsBean>> getAllClubs() {
		List<BookClubsBean> list = bookClubService.getAllClubs();
		return ResponseEntity.ok(list);
	}

	@GetMapping("/categories")
	public ResponseEntity<List<ClubCategoriesBean>> getAllCategories() {
		List<ClubCategoriesBean> list = bookClubService.getAllCategories();
		return ResponseEntity.ok(list);
	}

	@GetMapping("/clubs/{clubId}")
	public ResponseEntity<BookClubsBean> getClub(@PathVariable("clubId") Integer clubId) {
		if (clubId == null) {
			throw new BusinessException(400, "ID不可空白");
		}
		BookClubsBean club = bookClubService.getClub(clubId);
		if (club == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(club);

	}

	@PostMapping("/insert")
	public ResponseEntity<?> createClub(@RequestPart("bookclub") BookClubRequestDTO dto,
			@RequestPart(value = "proofFile", required = false) MultipartFile proof, HttpServletRequest request)
			throws IOException {
		Integer userId = (Integer) request.getAttribute("userId");

		if (userId == null) {
			return ResponseEntity.status(401).body("請先登入");
		}

		// 處理檔案上傳
		if (proof != null && !proof.isEmpty()) {
			String path = bookClubService.uploadFile(proof, "proof");
			dto.setProofPath(path);
		}

		BookClubsBean created = bookClubService.createBookClub(dto, userId);
		UserBean user = usersService.findById(userId);
		emailService.sendPendingMailToHost(user.getEmail(), created.getClubName(), user.getUserName());
		return ResponseEntity.ok(created);
	}

	@GetMapping("/my-hosted")
	public ResponseEntity<List<BookClubsBean>> getMyHostedClubs(HttpServletRequest request) {
		Integer userId = (Integer) request.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(401).build();
		}
		// Service 需要新增 findByHostId 方法，或在此處轉換
		// 暫時使用 findByHostId (假設 Service 已有此方法，或需新增)
		List<BookClubsBean> list = bookClubService.findByHostId(userId);
		return ResponseEntity.ok(list);
	}

	@DeleteMapping("/delete/{clubId}")
	public ResponseEntity<String> deleteClub(@PathVariable Integer clubId) {
		bookClubService.deleteClubId(clubId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/approve/{clubId}")
	public ResponseEntity<?> approveClub(@PathVariable Integer clubId, HttpServletRequest request) {
		Integer adminId = (Integer) request.getAttribute("userId");
		UserBean user = usersService.findById(adminId);
		// 權限檢查可在此做，或依賴 Security Filter (假設 /api/admin/** 或內部邏輯控制)
		// 這裡假設能進到後台 API 的都已經過初步驗證，Service 也可再由 Token Role 驗證
		BookClubsBean club = bookClubService.approveClub(clubId, adminId);
//		emailService.sendAccetToHost(club.getHost().getEmail(), club.getClubName(), user.getUserName());
		return ResponseEntity.ok(club);
	}

	@PutMapping("/reject/{clubId}")
	public ResponseEntity<?> rejectClub(@PathVariable Integer clubId,
			@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> body,
			HttpServletRequest request) {
		Integer adminId = (Integer) request.getAttribute("userId");
		UserBean host = usersService.findById(adminId);
		String reason = body.get("reason");
		BookClubsBean club = bookClubService.getClub(clubId);
		bookClubService.rejectClub(clubId, reason, adminId);
		emailService.sendRejectToHost(club.getHost().getEmail(), club.getClubName(), host.getUserName(), reason);
		return ResponseEntity.ok(club);
	}

	@PutMapping("/cancel/{clubId}")
	public ResponseEntity<?> cancelClub(@PathVariable Integer clubId, HttpServletRequest request) {
		Integer userId = (Integer) request.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(401).build();
		}
		BookClubsBean club = bookClubService.cancelClub(clubId, userId);
		return ResponseEntity.ok(club);
	}

	@PutMapping("/end/{clubId}")
	public ResponseEntity<?> endClub(@PathVariable Integer clubId, HttpServletRequest request) {
		Integer userId = (Integer) request.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(401).build();
		}
		BookClubsBean club = bookClubService.endClub(clubId, userId);
		return ResponseEntity.ok(club);
	}

	@PutMapping("/update/{clubId}")
	public ResponseEntity<?> updateClub(@PathVariable Integer clubId, @RequestPart("data") BookClubRequestDTO dto,
			@RequestPart(value = "proof", required = false) MultipartFile proofFile, HttpServletRequest request,
			@RequestHeader(value = "Authorization", required = false) String authHeader)
			throws IllegalStateException, IOException {

		Integer userId = (Integer) request.getAttribute("userId");
		// ... existing code ...
		Integer userRole = getRoleFromToken(authHeader);
		if (userRole == null) {
			userRole = ClubConstants.ROLE_MEMBER;
		}

		// 處理檔案上傳 (若有新檔案則更新)
		if (proofFile != null && !proofFile.isEmpty()) {
			String path = bookClubService.uploadFile(proofFile, "proof");
			dto.setProofPath(path);
		}

		BookClubsBean club = bookClubService.updateBookclub(clubId, dto, userId, userRole);

		return ResponseEntity.ok(club);

	}

	// 從JWT中解析出Token
	private Integer getRoleFromToken(String authHeader) {
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				// 使用您的 JwtUtil 讀取角色字串
				String roleStr = jwtUtil.getRole(token);

				// 將字串權限轉換為 Service 看得懂的整數常數
				// 假設 Token 裡的字串是 "SUPER_ADMIN", "ADMIN", "MEMBER"
				if ("SUPER_ADMIN".equals(roleStr)) {
					return ClubConstants.ROLE_SUPER_ADMIN; // 0
				} else if ("ADMIN".equals(roleStr)) {
					return ClubConstants.ROLE_ADMIN; // 1
				} else {
					return ClubConstants.ROLE_MEMBER; // 2 (預設為會員)
				}
			} catch (Exception e) {
				// Token 解析失敗，回傳 null
				return null;
			}
		}
		return null;
	}

}
