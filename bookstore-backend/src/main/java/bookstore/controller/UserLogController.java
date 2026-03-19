package bookstore.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bookstore.bean.UserLogBean;
import bookstore.service.UserLogService;
import bookstore.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class UserLogController {

	private final UserLogService userLogService;
	private final JwtUtil jwtUtil;
	
	@GetMapping("/list")
	public List<UserLogBean> getLogs(HttpServletRequest request) {
		try {
			String authHeader = request.getHeader("Authorization");
			String token = authHeader.substring(7);
			String currentUserId = jwtUtil.getMemberId(token);
			String currentRole = jwtUtil.getRole(token);
			
			return userLogService.getLogsByRole(currentRole, Integer.parseInt(currentUserId));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
