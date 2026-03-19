package bookstore.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.bean.UserLogBean;

public interface UserLogRepository extends JpaRepository<UserLogBean, Integer> {
	
	List<UserLogBean> findByAdminUser_UserId(Integer userId, Sort sort);
}
