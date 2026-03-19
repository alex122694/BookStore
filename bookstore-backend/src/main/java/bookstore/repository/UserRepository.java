package bookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bookstore.bean.UserBean;

@Repository
public interface UserRepository extends JpaRepository<UserBean, Integer> {
	
	// 後台會員系統
	UserBean findByEmailAndUserPwd(String email, String userPwd);
	List<UserBean> findByUserNameContainingAndUserType(String userName, Integer userType);
	List<UserBean> findByUserNameContaining(String userName);
	List<UserBean> findByUserType(Integer userType);
	
	@Modifying
	@Query("UPDATE UserBean u SET u.status = :status WHERE u.userId = :id")
	void updateStatus(@Param("id") Integer id, @Param("status") Integer status);
	
	@Query("SELECT u FROM UserBean u WHERE " +
		       "(:keyword IS NULL OR " +
		       " u.userName LIKE %:keyword% OR " +
		       " u.email LIKE %:keyword% OR " +
		       " u.phoneNum LIKE %:keyword%) AND " +
		       "(:type IS NULL OR u.userType = :type)")
		List<UserBean> searchUsersGlobal(
		    @Param("keyword") String keyword, 
		    @Param("type") Integer type);
	
	boolean existsByEmailAndUserIdNot(String email, Integer userId);
	boolean existsByPhoneNumAndUserIdNot(String phoneNum, Integer userId);
	boolean existsByEmail(String email);
	boolean existsByPhoneNum(String phoneNum);
	
	// 前台書店系統
	
	UserBean findByEmail(String email);
	
	@Query("SELECT u FROM UserBean u WHERE u.email = :email AND FUNCTION('FORMAT', u.birth, 'yyyy-MM-dd') = :birthStr")
	UserBean findByEmailAndBirthString(
	    @Param("email") String email, 
	    @Param("birthStr") String birth
	);
	
	
}
