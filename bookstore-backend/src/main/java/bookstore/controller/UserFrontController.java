package bookstore.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import bookstore.bean.UserBean;
import bookstore.service.EmailService;
import bookstore.service.UsersService;
import bookstore.util.JwtUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class UserFrontController {
	
	private final UsersService userService;
	private final JwtUtil jwtUtil;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	
	@PostMapping("/api/user/google-login")
	@ResponseBody
	@SuppressWarnings("unchecked")
	public Map<String, Object> googleLogin(@RequestBody Map<String, String> data, HttpSession session) {
		String accessToken = data.get("accessToken");
		Map<String, Object> response = new HashMap<>();
		
		try {
	        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken;
	        RestTemplate restTemplate = new RestTemplate();
	        Map<String, Object> googleUser = restTemplate.getForObject(userInfoUrl, Map.class);
	        
	        if (googleUser != null && googleUser.containsKey("email")) {
	            String email = (String) googleUser.get("email");
	            String name = (String) googleUser.get("name");
			
			UserBean user = userService.findByEmail(email);
			
			if (user != null) {
				if (user.getStatus() != null && user.getStatus() == 2) {
			        response.put("success", false);
			        response.put("message", "您的帳號已被停權，請聯繫管理員！");
			        return response;
			    }
				session.setAttribute("currentUserId", user.getUserId().toString());
				String role = (user.getUserType() == 0) ? "SUPER_ADMIN" : (user.getUserType() == 1 ? "ADMIN" : "USER");
				String token = jwtUtil.generateToken(user.getUserId().toString(), role);
				
				response.put("success", true);
				response.put("token", token);
				response.put("userName", user.getUserName());
				response.put("role", role);
				response.put("userId", user.getUserId());
				response.put("img", user.getImg());
			} else {
				response.put("success", false);
				response.put("isNewUser", true);
				response.put("email", email);
				response.put("userName", name);
				response.put("message", "請完成基本資料設定！");
				}
	        }
			
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "Google 驗證失敗");
		}
		return response;
	}
	
	@PostMapping("/api/user/login")
	@ResponseBody
	public Map<String, Object> login(@RequestBody Map<String, String> loginData, HttpSession session) {
	    Map<String, Object> response = new HashMap<>();
	    String email = loginData.get("email");
	    String rawPassword = loginData.get("userPwd");

	    UserBean user = userService.findByEmail(email);

	    if (user != null) {
	        if (passwordEncoder.matches(rawPassword, user.getUserPwd())) {
	        	
	        	if (user.getStatus() != null && user.getStatus() == 2) {
	                response.put("success", false);
	                response.put("message", "您的帳號已被停權，請聯繫管理員！");
	                return response;
	            }
	        	session.setAttribute("currentUserId", user.getUserId().toString());
	            String role = (user.getUserType() == 0) ? "SUPER_ADMIN" : (user.getUserType() == 1 ? "ADMIN" : "USER");
	            String token = jwtUtil.generateToken(user.getUserId().toString(), role);

	            response.put("success", true);
	            response.put("token", token);
	            response.put("userName", user.getUserName());
	            response.put("role", role);
	            response.put("message", "歡迎回來！");
	            response.put("userId", user.getUserId());
	            response.put("img", user.getImg());
	            System.out.println("使用者 " + user.getUserName() + " 登入，頭貼路徑為: " + user.getImg());
	        } else {
	            response.put("success", false);
	            response.put("message", "密碼錯誤！");
	        }
	    } else {
	        response.put("success", false);
	        response.put("message", "帳號不存在！");
	    }
	    return response;
	}
	
	@PostMapping("/api/user/register")
	@ResponseBody
	public Map<String, Object> userRegister(@RequestBody Map<String, Object> payload, HttpSession session){
		Map<String, Object> response = new HashMap<>();
		
		String email = (String) payload.get("email");
		String inputCode = (String) payload.get("verifyCode");
		
		String sessionCode = (String) session.getAttribute("verifyCode_" + email);
		if (sessionCode == null || !sessionCode.equals(inputCode)) {
			response.put("success", false);
			response.put("message", "驗證碼錯誤或已過期！");
			return response;
		}
		
		String phoneNum = (String) payload.get("phoneNum");
		Map<String, Object> uniqueCheck = userService.checkUserUnique(null, email, phoneNum);
		if (!(boolean) uniqueCheck.get("success")) {
			return uniqueCheck;
		}
		
		String userName = (String) payload.get("userName");
		String userPwd = (String) payload.get("userPwd");
		
		if (email == null || email.isEmpty() || userName == null || userName.isEmpty() ||
				userPwd == null || userPwd.isEmpty()) {
			response.put("success", false);
			response.put("message", "新增失敗！Email、姓名與密碼必須填入！");
			return response;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		UserBean user = new UserBean();
		user.setEmail(email);
		user.setUserName(userName);
		user.setUserPwd(passwordEncoder.encode(userPwd));
		user.setPhoneNum(phoneNum);
		String birthStr = (String) payload.get("birth");
		if (birthStr != null && !birthStr.isEmpty()) {
			try {
				user.setBirth(sdf.parse(birthStr));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
		}
		user.setGender((String) payload.get("gender"));
		user.setAddress((String) payload.get("address"));
		
		Integer userType = (Integer) payload.get("userType");
		user.setUserType(userType == null ? 2 : user.getUserType());
	    user.setStatus(1);
	    user.setPoints(0);
	    
	    try {
			userService.saveUser(user);
			session.removeAttribute("verifyCode_" + email);
			response.put("success", true);
			response.put("message", "註冊成功！");
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "註冊失敗！");
		}
		return response;
	}
	
	@PostMapping("/api/user/send-code")
	@ResponseBody
	public Map<String, Object> sendCode(@RequestBody Map<String, String> data, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		String email = data.get("email");
		String type = data.getOrDefault("type", "register");
		
		if (email == null || email.isEmpty()) {
			response.put("success", false);
			response.put("message", "請輸入電子信箱！");
			return response;
		}
		
		String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
		session.setAttribute("verifyCode_" + email, code);
		
		try {
			String subject = type.equals("forget") ? "森林書屋 - 密碼重設驗證碼" : "森林書屋 - 會員註冊驗證碼";
			
			emailService.sendVerifyCode(email, code, subject);
			response.put("success", true);
			response.put("message", "驗證碼已寄出！");
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "郵件寄送失敗，請檢查設定！");
		}
		return response;
	}
	
	@GetMapping("/api/user/check-unique")
	@ResponseBody
	public Map<String, Object> checkUnique(@RequestParam(required = false) String email, @RequestParam(required = false) String phone) {
		return userService.checkUserUnique(null, email, phone);
	}
	
	@PostMapping("/api/user/forget-password")
	@ResponseBody
	public Map<String, Object> forgetPasswordByEmail(@RequestBody Map<String, String> data, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		String email = (String) data.get("email");
		String birth = (String) data.get("birth");
		
		UserBean findUser = userService.findByEmailAndBirth(email, birth);
		
		if (findUser != null) {
			String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
			session.setAttribute("verifyCode_" + email, code);
			
			try {
				emailService.sendVerifyCode(email, code, "森林書屋 - 密碼重設驗證碼");
				response.put("success", true);
				response.put("userId", findUser.getUserId());
				response.put("email", email);
				response.put("message", "驗證成功，驗證碼已寄出！");
			} catch (Exception e) {
				response.put("success", false);
				response.put("message", "身分正確但郵件發送失敗，請稍後再試！");
			}
		} else {
			response.put("success", false);
			response.put("message", "Email 或 生日錯誤！");
		}
		return response;
	}
	
	@PostMapping("/api/user/verify-reset-code")
	@ResponseBody
	public Map<String, Object> verifyResetPasswordCode(@RequestBody Map<String, String> data, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		String email = data.get("email");
		String inputCode = data.get("verifyCode");
		String userId = data.get("userId");
		
		String sessionCode = (String) session.getAttribute("verifyCode_" + email);
		if (sessionCode != null && sessionCode.equals(inputCode)) {
			response.put("success", true);
			response.put("message", "驗證成功！");
			response.put("userId", userId);
			
			String resetToken = UUID.randomUUID().toString();
			session.setAttribute("resetToken_" + userId, resetToken);
			response.put("resetToken", resetToken);
			
			session.removeAttribute("verifyCode_" + email);
		} else {
			response.put("success", false);
			response.put("message", "驗證碼錯誤或已過期！");
		}
		return response;
	}
	
	@PostMapping("/api/user/do-reset-password")
	@ResponseBody
	public Map<String, Object> resetPassword(@RequestBody Map<String, String> data, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		
		String userId = data.get("userId");
		String newPassword = data.get("newPassword");
		String resetToken = data.get("resetToken");
		
		String sessionToken = (String) session.getAttribute("resetToken_" + userId);
		
		if (sessionToken == null || !sessionToken.equals(resetToken)) {
			response.put("success", false);
			response.put("message", "安全驗證無效或已過期，請重新操作！");
			return response;
		}
		
		try {
			UserBean user = userService.findById(Integer.parseInt(userId));
			if (user != null) {
				user.setUserPwd(passwordEncoder.encode(newPassword));
				userService.saveUser(user);
				
				emailService.sendResetSuccessNotification(user.getEmail());
				
				session.removeAttribute("resetToken_" + userId);
				
				response.put("success", true);
				response.put("message", "密碼修改成功！");
			} else {
				response.put("success", false);
				response.put("message", "找不到該會員資料！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "資料庫更新失敗！");
		}
		return response;
	}
	
	@PostMapping("/api/user/confirm-password")
	@ResponseBody
	public Map<String, Object> passwordConfirmation(@RequestBody Map<String, String> data, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		
		String rawPassword = data.get("password");
		String userId = (String) session.getAttribute("currentUserId");
		
		if (userId == null) {
			response.put("success", false);
			response.put("message", "連線逾時，請重新登入");
			return response;
		} 
		
		try {
			UserBean user = userService.findById(Integer.parseInt(userId));
			
			if (user != null && passwordEncoder.matches(rawPassword, user.getUserPwd())) {
				response.put("success", true);
				response.put("message", "驗證成功！");
			} else {
				response.put("success", false);
				response.put("message", "驗證失敗，密碼錯誤！");
			}
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "伺服器異常");
		}
		return response;
	}
	
	@PostMapping("/api/user/update-profile")
	@ResponseBody
	public Map<String, Object> updateUserProfile(@RequestParam String gender, @RequestParam String email,
			@RequestParam String phoneNum, @RequestParam String address,
			@RequestParam(value = "img", required = false) MultipartFile img, HttpSession session) {
		
		Map<String, Object> response = new HashMap<>();
		
		String userId = (String) session.getAttribute("currentUserId");
		
		if (userId == null) {
			response.put("success", false);
			response.put("message", "請重新登入！");
			return response;
		}
		
		try {
			UserBean user = userService.findById(Integer.parseInt(userId));
			
			user.setGender(gender);
			user.setEmail(email);
			user.setPhoneNum(phoneNum);
			user.setAddress(address);
			
			if (img != null && !img.isEmpty()) {
				String uploadDir = "C:/uploads/avatars/";
	            String fileName = "user_" + userId + "_" + System.currentTimeMillis() + ".png";
	            
	            File dir = new File(uploadDir);
	            if (!dir.exists()) dir.mkdirs();
	            
	            img.transferTo(new java.io.File(uploadDir + fileName));
	            
	            user.setImg("/uploads/avatars/" + fileName); 
	            response.put("newImg", "/uploads/avatars/" + fileName);
	        }
				
			userService.saveUser(user);
			response.put("success", true);
			response.put("message", "個人資料已成功更新！");
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "資料寫入異常！");
		}
		return response;
	}
	
	@GetMapping("/api/user/my-profile")
	@ResponseBody
	public Map<String, Object> getProfile(HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		
		String userId = (String) session.getAttribute("currentUserId");
		
		if (userId == null) {
			response.put("success", false);
			response.put("message", "請先登入！");
			return response;
		}
		
		try {
			UserBean user = userService.findById(Integer.parseInt(userId));
			
			if (user != null) {
				user.setUserPwd(null);
				response.put("success", true);
				response.put("user", user);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "伺服器異常");
		}
		return response;
	}
	
	@PostMapping("/api/user/change-password")
	@ResponseBody
	public Map<String, Object> changePassword(@RequestBody Map<String, String> data, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		
		String userId = (String) session.getAttribute("currentUserId");
		String oldPwd = data.get("oldPwd");
		String newPwd = data.get("newPwd");
		
		if (userId == null) {
			response.put("success", false);
			response.put("message", "請重新登入！");
			return response;
		}
		
		try {
			UserBean user = userService.findById(Integer.parseInt(userId));
			
			if (passwordEncoder.matches(oldPwd, user.getUserPwd())) {
				user.setUserPwd(passwordEncoder.encode(newPwd));
				userService.saveUser(user);
					
				emailService.sendResetSuccessNotification(user.getEmail());
					
				response.put("success", true);
				response.put("message", "密碼修改成功！");
			} else {
				response.put("success", false);
				response.put("message", "原密碼輸入錯誤");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "系統處理失敗！");
		}
		return response;
		
	}
}