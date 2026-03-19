package bookstore.controller;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import bookstore.bean.UserBean;
import bookstore.service.UserLogService;
import bookstore.service.UsersService;
import bookstore.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")

public class UserController {

	private final UsersService userService;
	private final JwtUtil jwtUtil;
	private final UserLogService userLogService;
	private final PasswordEncoder passwordEncoder;

	public UserController(UsersService userService, JwtUtil jwtUtil, UserLogService userLogService,
			PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.jwtUtil = jwtUtil;
		this.userLogService = userLogService;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/api/login")
	@ResponseBody
	public Map<String, Object> doLoginApi(@RequestBody Map<String, String> loginData) {

		String email = loginData.get("email");
		String password = loginData.get("password");
		Map<String, Object> response = new HashMap<>();

		if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
			response.put("success", false);
			response.put("message", "信箱和密碼欄位皆不可為空！");
			return response;
		}

		UserBean user = userService.findByEmail(email);

		if (user != null && passwordEncoder.matches(password, user.getUserPwd())) {
			if (user.getStatus() != null && user.getStatus().equals(2)) {
				response.put("success", false);
				response.put("message", "您的帳號已被停權，請聯繫管理員！");
				return response;
			}

			if (user.getUserType() == 2) {
				response.put("success", false);
				response.put("message", "您無權進入後台系統！");
				return response;
			}

			String role = (user.getUserType() == 0) ? "SUPER_ADMIN" : "ADMIN";

			String token = "";
			try {
				token = jwtUtil.generateToken(user.getUserId().toString(), role);
			} catch (Exception e) {
				e.printStackTrace();
				response.put("success", false);
				response.put("message", "系統生成證件失敗！");
				return response;
			}
			response.put("success", true);
			response.put("message", "登入成功！");
			response.put("token", token);
			response.put("role", role);
			response.put("userName", user.getUserName());
			response.put("userId", user.getUserId());
			return response;
		} else {
			response.put("success", false);
			response.put("message", "帳號或密碼錯誤！");
			return response;
		}
	}

	@GetMapping("/api/data/list")
	@ResponseBody
	public List<UserBean> getUsersListApi(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Integer userTypeFilter) {

		try {
			String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;

			List<UserBean> users = userService.searchUsers(cleanKeyword, userTypeFilter);
			return users;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@GetMapping("/api/data/get/{id}")
	@ResponseBody
	public UserBean getUserDetailApi(@PathVariable("id") Integer id) {
		try {
			UserBean user = userService.findById(id);
			if (user == null) {
				return null;
			}
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@PostMapping("/api/users/insert")
	@ResponseBody
	public Map<String, Object> processInsertApi(@RequestBody UserBean user, HttpServletRequest request) {
		Map<String, Object> uniqueCheck = userService.checkUserUnique(null, user.getEmail(), user.getPhoneNum());
		if (!(boolean) uniqueCheck.get("success")) {
			return uniqueCheck;
		}

		Map<String, Object> response = new HashMap<>();

		if (user.getEmail() == null || user.getEmail().isEmpty() ||
				user.getUserPwd() == null || user.getUserPwd().isEmpty() ||
				user.getUserName() == null || user.getUserName().isEmpty()) {
			response.put("success", false);
			response.put("message", "新增失敗！信箱、密碼與姓名必須填入！");
			return response;
		}

		if (user.getUserType() != null && user.getUserType() == 2) {
			response.put("success", false);
			response.put("message", "新增失敗！後台系統禁止直接建立一般會員，請引導使用者至前台註冊！");
			return response;
		}

		if (user.getUserType() == null) {
			user.setUserType(1);
		}

		user.setStatus(1);
		user.setPoints(0);

		String encodedPassword = passwordEncoder.encode(user.getUserPwd());
		user.setUserPwd(encodedPassword);

		try {
			userService.saveUser(user);

			String authHeader = request.getHeader("Authorization");
			String token = authHeader.substring(7);
			String adminId = jwtUtil.getMemberId(token);
			UserBean adminOperator = userService.findById(Integer.parseInt(adminId));

			userLogService.recordAction(adminOperator, "新增會員:「" + user.getUserName() + "」",
					user.getUserId().toString());

			response.put("success", true);
			response.put("message", "新增會員資料成功！");
			response.put("user", user);
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "新增失敗！資料庫錯誤，請檢查 Email 是否重複！");
		}
		return response;
	}

	@GetMapping("/api/users/check-unique")
	@ResponseBody
	public Map<String, Object> checkUniqueApi(@RequestParam(required = false) Integer userId,
			@RequestParam(required = false) String email, @RequestParam(required = false) String phoneNum) {
		return userService.checkUserUnique(userId, email, phoneNum);
	}

	@PutMapping("/api/data/status/{id}")
	@ResponseBody
	public Map<String, Object> toggleUserStatus(@PathVariable("id") Integer id,
			@RequestBody Map<String, Integer> payload, HttpServletRequest request) {
		Map<String, Object> response = new HashMap<>();
		try {
			Integer newStatus = payload.get("status");

			String authHeader = request.getHeader("Authorization");
			String token = authHeader.substring(7);
			String currentOperUserId = jwtUtil.getMemberId(token);

			if (id.toString().equals(currentOperUserId) && newStatus != null && newStatus == 2) {
				response.put("success", false);
				response.put("message", "更新失敗：您無法停權自己的帳號！");
				return response;
			}

			if (newStatus == null) {
				response.put("success", false);
				response.put("message", "狀態碼不可為空！");
				return response;
			}
			userService.updateStatus(id, newStatus);

			try {
				UserBean adminOperator = userService.findById(Integer.parseInt(currentOperUserId));

				UserBean targetUser = userService.findById(id);

				String actionVerb = (newStatus == 2) ? "停權會員" : "啟用會員";
				String logAction = String.format("%s:「%s」", actionVerb, targetUser.getUserName());

				userLogService.recordAction(adminOperator, logAction, id.toString());
			} catch (Exception logEx) {
				System.err.println("狀態變更日誌紀錄失敗: " + logEx.getMessage());
			}

			response.put("success", true);
			String action = (newStatus == 2) ? "停權" : "啟用";
			response.put("message", "已成功將會員" + action + "！");
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "狀態變更失敗！系統錯誤");
		}
		return response;
	}

	@PutMapping("/api/data/update")
	@ResponseBody
	public Map<String, Object> processUpdateApi(@RequestBody UserBean user, HttpServletRequest request) {
		Map<String, Object> response = new HashMap<>();

		Map<String, Object> uniqueCheck = userService.checkUserUnique(user.getUserId(), user.getEmail(),
				user.getPhoneNum());
		if (!(boolean) uniqueCheck.get("success")) {
			return uniqueCheck;
		}

		try {
			String authHeader = request.getHeader("Authorization");
			String token = authHeader.substring(7);
			String currentUserId = jwtUtil.getMemberId(token);
			String currentRole = jwtUtil.getRole(token);

			UserBean existingUser = userService.findById(user.getUserId());
			if (existingUser == null) {
				response.put("success", false);
				response.put("message", "更新失敗：找不到該會員！");
				return response;
			}

			if ("ADMIN".equals(currentRole)) {
				if ((existingUser.getUserType() == 0 || existingUser.getUserType() == 1)
						&& !existingUser.getUserId().toString().equals(currentUserId)) {
					response.put("success", false);
					response.put("message", "權限不足：您無法修改其他管理員的資料！");
					return response;
				}

				if (user.getUserType() != null && user.getUserType() != existingUser.getUserType()) {
					if (existingUser.getUserType() == 2 && (user.getUserType() == 0 || user.getUserType() == 1)) {
						response.put("success", false);
						response.put("message", "權限不足：一般管理員無法提升會員權限！");
						return response;
					}
					if ((existingUser.getUserType() == 0 || existingUser.getUserType() == 1)
							&& user.getUserType() == 2) {
						response.put("success", false);
						response.put("message", "權限不足：您無法將管理員降級為一般會員！");
						return response;
					}
				}
			}

			StringBuilder diff = new StringBuilder();

			if (!Objects.equals(existingUser.getEmail(), user.getEmail())) {
				diff.append(String.format("Email 從『%s』變更為『%s』｜", existingUser.getEmail(), user.getEmail()));
			}

			if (user.getUserPwd() != null && !user.getUserPwd().trim().isEmpty()) {
				String encodedPwd = passwordEncoder.encode(user.getUserPwd().trim());
				user.setUserPwd(encodedPwd);
				diff.append("密碼變更｜");
			} else {
				user.setUserPwd(existingUser.getUserPwd());
			}

			if (!Objects.equals(existingUser.getUserName(), user.getUserName())) {
				diff.append(String.format("姓名從『%s』變更為『%s』｜", existingUser.getUserName(), user.getUserName()));
			}

			if (!Objects.equals(existingUser.getGender(), user.getGender())) {
				String oldG = "M".equals(existingUser.getGender()) ? "男" : "女";
				String newG = "M".equals(user.getGender()) ? "男" : "女";
				diff.append(String.format("性別從『%s』變更為『%s』｜", oldG, newG));
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String oldBirthStr = (existingUser.getBirth() != null) ? sdf.format(existingUser.getBirth()) : "未填";
			String newBirthStr = (user.getBirth() != null) ? sdf.format(user.getBirth()) : "未填";

			if (!oldBirthStr.equals(newBirthStr)) {
				diff.append(String.format("生日從『%s』變更為『%s』｜", oldBirthStr, newBirthStr));
			}

			if (!Objects.equals(existingUser.getPhoneNum(), user.getPhoneNum())) {
				diff.append(String.format("聯絡電話從『%s』變更為『%s』｜", existingUser.getPhoneNum(), user.getPhoneNum()));
			}

			if (!Objects.equals(existingUser.getAddress(), user.getAddress())) {
				diff.append(String.format("地址從『%s』變更為『%s』｜", existingUser.getAddress(), user.getAddress()));
			}

			if (!Objects.equals(existingUser.getUserType(), user.getUserType())) {
				String[] roles = { "超級管理員", "一般管理員", "一般會員" };
				String oldR = roles[existingUser.getUserType()];
				String newR = roles[user.getUserType()];
				diff.append(String.format("權限從『%s』變更為『%s』｜", oldR, newR));
			}

			if (user.getPoints() == null) {
				user.setPoints(existingUser.getPoints());
			}
			if (user.getStatus() == null) {
				user.setStatus(existingUser.getStatus());
			}

			userService.saveUser(user);

			try {
				UserBean adminOperator = userService.findById(Integer.parseInt(currentUserId));
				String logAction;

				if (diff.length() > 0) {
					logAction = String.format("將「%s」的 %s ", existingUser.getUserName(), diff.toString().trim());
				} else {
					logAction = String.format("查看並儲存會員: %s （內容未變動）", existingUser.getUserName());
				}

				userLogService.recordAction(adminOperator, logAction, user.getUserId().toString());
			} catch (Exception logEx) {
				System.err.println("日誌紀錄失敗: " + logEx.getMessage());
			}

			response.put("success", true);
			response.put("message", "會員資料更新成功！");

		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "更新失敗：伺服器發生錯誤！");
		}
		return response;
	}

	@DeleteMapping("/api/users/delete/{id}")
	@ResponseBody
	public Map<String, Object> deleteUser(@PathVariable("id") Integer id) {
		Map<String, Object> response = new HashMap<>();
		try {
			UserBean user = userService.findById(id);
			if (user != null) {
				userService.deleteUser(id);
				response.put("success", true);
				response.put("message", "刪除會員成功！");
			} else {
				response.put("success", false);
				response.put("message", "找無此會員，刪除失敗！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "刪除失敗：伺服器發生錯誤！");
		}
		return response;
	}

}
