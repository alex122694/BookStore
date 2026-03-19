package bookstore.dao;

import java.util.List;
import bookstore.bean.UserBean;

public interface UserDAO {
	public List<UserBean> selectAllUsers(String searchName, Integer userTypeFilter);
	public UserBean selectUserByCredentials(String email, String password);
	public UserBean selectUserById(Integer userId);
	public int insertUser(UserBean user);
	public int updateUser(UserBean user);
	public int deleteUser(Integer userId);
}

