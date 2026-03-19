package bookstore.dao.impl;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import bookstore.bean.UserBean;
import bookstore.dao.UserDAO;
import bookstore.util.HibernateUtil;

public class UsersDAOImpl implements UserDAO {
	
	private SessionFactory factory;
	
	public UsersDAOImpl() {
		this.factory = HibernateUtil.getSessionFactory();
	}

	@Override
	public List<UserBean> selectAllUsers(String searchName, Integer userTypeFilter) {
		Session session = factory.getCurrentSession();
		StringBuilder hql = new StringBuilder("FROM UserBean WHERE 1=1");
		if (searchName != null && !searchName.trim().isEmpty()) {
			hql.append(" AND userName LIKE :name");
		}
		if (userTypeFilter != null) {
			hql.append(" AND userType = :type");
		}
		Query<UserBean> query = session.createQuery(hql.toString(), UserBean.class);
		if (searchName != null && !searchName.trim().isEmpty()) {
			query.setParameter("name", searchName + "%");
		}
		if (userTypeFilter != null) {
			query.setParameter("type", userTypeFilter);
		}
		return query.getResultList();
	}
	
	@Override
	public UserBean selectUserByCredentials(String email, String password) {
		Session session = factory.getCurrentSession();
		String hql = "FROM UserBean WHERE email = :email AND userPwd = :pwd";
		return session.createQuery(hql, UserBean.class).setParameter("email", email).setParameter("pwd", password).uniqueResult();
	}

	@SuppressWarnings("removal")
	@Override
	public UserBean selectUserById(Integer userId) {
		return factory.getCurrentSession().get(UserBean.class, userId);
	}

	@Override
	public int insertUser(UserBean user) {
		try {
			factory.getCurrentSession().persist(user);
			return 1;
		} catch (HibernateException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@SuppressWarnings("removal")
	@Override
	public int updateUser(UserBean user) {
		try {
			Session session = factory.getCurrentSession();
			UserBean existingUser = session.get(UserBean.class, user.getUserId());
			if (existingUser != null) {
				existingUser.setEmail(user.getEmail());
			    existingUser.setUserPwd(user.getUserPwd());
			    existingUser.setUserName(user.getUserName());
			    existingUser.setGender(user.getGender());
			    existingUser.setBirth(user.getBirth());
			    existingUser.setPhoneNum(user.getPhoneNum());
			    existingUser.setAddress(user.getAddress());
			    existingUser.setUserType(user.getUserType());
			    return 1;
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@SuppressWarnings("removal")
	@Override
	public int deleteUser(Integer userId) {
		Session session = factory.getCurrentSession();
		UserBean user = session.get(UserBean.class, userId);
		if (user != null) {
			session.remove(user);
			return 1;
		}
		return 0;
	}
}
