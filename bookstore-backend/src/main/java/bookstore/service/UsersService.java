package bookstore.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bookstore.bean.UserBean;
import bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {

	private final UserRepository userRepo;

	// 後台會員系統
	public UserBean login(String email, String password) {
		if (email == null || password == null)
			return null;
		return userRepo.findByEmailAndUserPwd(email, password);
	}

	public List<UserBean> findAllUsers() {
		return userRepo.findAll();
	}

	public List<UserBean> searchUsers(String keyword, Integer type) {
		if ((keyword == null || keyword.trim().isEmpty()) && type == null) {
			return userRepo.findAll();
		}
		return userRepo.searchUsersGlobal(keyword, type);
	}

	public UserBean findById(Integer id) {
		return userRepo.findById(id).orElse(null);
	}

	public UserBean saveUser(UserBean user) {
	    if (user.getImg() != null) {
	        String processedPath = saveBase64Image(user.getImg(), user.getUserId());
	        user.setImg(processedPath);
	    }
	    return userRepo.save(user);
	}

	public void deleteUser(Integer id) {
		userRepo.deleteById(id);
	}

	public void updateStatus(Integer id, Integer status) {
		UserBean user = userRepo.findById(id).orElse(null);
		if (user != null) {
			user.setStatus(status);
			userRepo.save(user);
		}
	}

	public Map<String, Object> checkUserUnique(Integer userId, String email, String phoneNum) {
		Map<String, Object> result = new HashMap<>();
		result.put("success", true);

		if (userId == null) {
			if (userRepo.existsByEmail(email)) {
				result.put("success", false);
				result.put("message", "該 Email 已被註冊！");
				return result;
			}
			if (phoneNum != null && !phoneNum.isEmpty() && userRepo.existsByPhoneNum(phoneNum)) {
				result.put("success", false);
				result.put("message", "該手機號碼已被使用！");
				return result;
			}
		} else {
			if (userRepo.existsByEmailAndUserIdNot(email, userId)) {
				result.put("success", false);
				result.put("message", "Email 已被其他會員使用！");
				return result;
			}
			if (phoneNum != null && !phoneNum.isEmpty() && userRepo.existsByPhoneNumAndUserIdNot(phoneNum, userId)) {
				result.put("success", false);
				result.put("message", "手機號碼已被其他會員使用！");
				return result;
			}
		}
		return result;
	}

	// 前台書店系統

	public UserBean findByEmail(String email) {
		return userRepo.findByEmail(email);
	}

	public UserBean findByEmailAndBirth(String email, String birth) {
		return userRepo.findByEmailAndBirthString(email, birth);
	}

	
	@Value("${file.avatar-dir}")
	private String avatarDir; 

	private String saveBase64Image(String base64Data, Integer userId) {
	    if (base64Data == null || !base64Data.startsWith("data:image")) {
	        return base64Data;
	    }

	    try {
	        String[] parts = base64Data.split(",");
	        String header = parts[0]; 
	        String extension = header.contains("png") ? "png" : "jpg";
	        byte[] imageBytes = java.util.Base64.getDecoder().decode(parts[1]);

	        java.io.File dir = new java.io.File(avatarDir);
	        if (!dir.exists()) {
	            dir.mkdirs();
	        }

	        String fileName = "user_" + userId + "_" + System.currentTimeMillis() + "." + extension;
	        
	        java.nio.file.Path path = java.nio.file.Paths.get(avatarDir, fileName);
	        java.nio.file.Files.write(path, imageBytes);

	        return "/uploads/avatars/" + fileName; 
	    } catch (Exception e) {
	        System.err.println("圖片存檔失敗: " + e.getMessage());
	        return null; 
	    }
	}
	
}
