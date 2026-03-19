package com.bookstore.user;

import java.util.List;
import com.bookstore.user.UserBean;

public interface UserDAO {
	public List<UserBean> selectAllUsers(String searchName, Integer userTypeFilter);
	public UserBean selectUserByCredentials(String email, String password);
	public UserBean selectUserById(Integer userId);
	public int insertUser(UserBean user);
	public int updateUser(UserBean user);
	public int deleteUser(Integer userId);
}

