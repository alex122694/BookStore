package com.bookstore.user;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bookstore.user.UserLogBean;

public interface UserLogRepository extends JpaRepository<UserLogBean, Integer> {
	
	List<UserLogBean> findByAdminUser_UserId(Integer userId, Sort sort);
}
