package com.bookstore.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.bookstore.user.UserBean;
import com.bookstore.user.UserLogBean;
import com.bookstore.user.UserLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserLogService {
	
	private final UserLogRepository logRepo;
	
	public void recordAction(UserBean admin, String action, String targetId) {
		UserLogBean log = new UserLogBean();
		log.setAdminUser(admin);
		log.setAdminName(admin.getUserName());
		log.setAction(action);
		log.setTargetId(targetId);
		log.setActionTime(LocalDateTime.now());
		
		logRepo.save(log);
	}
	
	public List<UserLogBean> getLogsByRole(String role, Integer userId) {
	    Sort sortByTime = Sort.by(Sort.Direction.DESC, "actionTime");
	    
	    if ("SUPER_ADMIN".equals(role)) {
	        return logRepo.findAll(sortByTime);
	    } else {
	        return logRepo.findByAdminUser_UserId(userId, sortByTime);
	    }
	}
	
}
