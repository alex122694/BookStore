package bookstore.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;

import bookstore.aop.BusinessException;
import bookstore.bean.BookClubsBean;
import bookstore.bean.ClubRegistrationsBean;
import bookstore.bean.UserBean;
import bookstore.service.BookClubService;
import bookstore.service.ClubRegistrationService;
import bookstore.service.EmailService;
import bookstore.service.UsersService;
import bookstore.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/reg")
public class ClubRegistrationController {

	private final EmailService emailService;

	@Autowired
	private ClubRegistrationService regService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UsersService usersService;

	@Autowired
	private BookClubService bookClubService;

	ClubRegistrationController(EmailService emailService) {
		this.emailService = emailService;
	}

	// 取得所有 (管理員用?)
	@GetMapping("/reglist")
	public ResponseEntity<List<ClubRegistrationsBean>> getAllReg() {
		List<ClubRegistrationsBean> list = regService.getAllRegistrations();
		if (list == null || list.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(list);
	}

	@GetMapping("/{regId}")
	public ResponseEntity<ClubRegistrationsBean> getReg(@PathVariable("regId") Integer regId) {
		if (regId == null) {
			throw new BusinessException(400, "報名表ID不可為空白");
		}
		ClubRegistrationsBean reg = regService.getRegistrationById(regId);
		return ResponseEntity.ok(reg);
	}

	@GetMapping("/club/{clubId}")
	public ResponseEntity<List<ClubRegistrationsBean>> getRegistrationsByClub(@PathVariable("clubId") Integer clubId) {
		if (clubId == null) {
			throw new BusinessException(400, "讀書會ID不可為空白");
		}
		System.out.println("收到查詢報名請求, ClubId: " + clubId);
		List<ClubRegistrationsBean> regList = regService.getRegistrationsByClubId(clubId);
		System.out.println("查詢結果筆數: " + (regList != null ? regList.size() : "null"));

		if (regList != null) {
			for (ClubRegistrationsBean reg : regList) {
				// 強制初始化 User 以確保序列化正常 (若 JOIN FETCH 失效)
				if (reg.getUser() != null) {
					reg.getUser().getUserName();
				}
			}
		}

		return ResponseEntity.ok(regList);
	}

	@GetMapping("/my-registrations")
	public ResponseEntity<List<ClubRegistrationsBean>> getMyRegistrations(HttpServletRequest request) {
		Integer userId = getUserIdFromToken(request);
		List<ClubRegistrationsBean> regList = regService.getRegistrationByUserId(userId);
		return ResponseEntity.ok(regList);
	}

	@PostMapping("/register/{clubId}")
	public ResponseEntity<ClubRegistrationsBean> register(@PathVariable("clubId") Integer clubId,
			HttpServletRequest request) {
		Integer userId = getUserIdFromToken(request);
		UserBean member = usersService.findById(userId);
		UserBean host = usersService.findById(clubId);
		BookClubsBean club = bookClubService.getClub(clubId);
		ClubRegistrationsBean registration = regService.register(clubId, userId);
		emailService.sendRegistrationToMember(member.getEmail(), registration.getBookClub().getClubName(),
				registration.getBookClub().getLocation(), registration.getBookClub().getHost(),
				registration.getBookClub().getEventDate(), member.getUserName());
		return ResponseEntity.ok(registration);
	}

	@PutMapping("/checkin/{clubId}/{targetUserId}")
	public ResponseEntity<?> checkIn(@PathVariable("clubId") Integer clubId,
			@PathVariable("targetUserId") Integer targetUserId) {
		// 未來可加入權限驗證: 只有 Host 能幫人報到
		regService.updateUserCheckIn(clubId, targetUserId);
		return ResponseEntity.ok("報到成功");
	}

	@PutMapping("/cancel/{clubId}")
	public ResponseEntity<?> cancel(@PathVariable("clubId") Integer clubId, HttpServletRequest request) {
		Integer userId = getUserIdFromToken(request);
		regService.updateCancel(clubId, userId);
		return ResponseEntity.ok("已取消報名");

	}

	// Deprecated endpoints logic removed or redirected

	private Integer getUserIdFromToken(HttpServletRequest request) {
		// 1. 嘗試從 Attribute 取得 (JwtInterceptor 已設定)
		Object userIdObj = request.getAttribute("userId");
		if (userIdObj != null) {
			return (Integer) userIdObj;
		}

		// 2. Fallback: 手動解析 (符合使用者要求參考 JwtUtil)
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				String idStr = jwtUtil.getMemberId(token);
				if (idStr != null) {
					return Integer.parseInt(idStr);
				}
			} catch (Exception e) {
				// Token 無效
			}
		}
		throw new BusinessException(401, "未登入或 Token 無效");
	}

}
