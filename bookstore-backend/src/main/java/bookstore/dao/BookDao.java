//package bookstore.dao;
//
//import java.math.BigDecimal;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
//import org.hibernate.query.Query;
//
//import bookstore.bean.BooksBean;
//import bookstore.bean.GenreBean;
//import bookstore.bean.OrderItem;
//import bookstore.bean.ReviewBean;
//import bookstore.util.DBUtil;
//import bookstore.util.HibernateUtil;
//
//public class BookDao {
//
//	SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
//
//	// select all books-------
////	public List<BooksBean> selectAllBooks() {
////		Session session = sessionFactory.getCurrentSession();
////		try {
////			String hql = "from BooksBean b left join fetch b.genreBean";
////			Query<BooksBean> query = session.createQuery(hql, BooksBean.class);
////			List<BooksBean> bookList = query.getResultList();
////			return bookList;
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
////		return null;
////	}
//
//	// select books by Id--------------------
////	public BooksBean selectBooksById(Integer bookId) {
////		Session session = sessionFactory.getCurrentSession();
////		try {
////			BooksBean book = session.find(BooksBean.class, bookId);
////			return book;
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
////		return null;
////	}
//
//	// select genre by Id--------------------
//	public GenreBean selectGenreById(Integer genreId) {
//		Session session = sessionFactory.getCurrentSession();
//		try {
//			GenreBean genre = session.find(GenreBean.class, genreId);
//			return genre;
//		} catch (Exception e) {
//
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	// select books by genres
//	public List<BooksBean> selectBooksByGenres(Integer genres) {
//		return null;
//	}
//
//	// select by isbn------
//	public BooksBean selectBooksByIsbn(String isbn) {
//		Session session = sessionFactory.getCurrentSession();
//
//		String hql1 = "from BooksBean b where b.isbn = :isbn";
//		BooksBean book = session.createQuery(hql1, BooksBean.class).setParameter("isbn", isbn).uniqueResult();
//
//		return book;
//	}
//
//	// select by onShelf-----
//	public Boolean selectOnShelfById(Integer bookId) {
//		Session session = sessionFactory.getCurrentSession();
//		String hql = "SELECT b.onShelf FROM BooksBean b WHERE b.bookId = :id";
//		Boolean onShelf = (Boolean) session.createQuery(hql).setParameter("id", bookId).uniqueResult();
//		return onShelf;
//	}
//
//	// delete驗證，若評價有該書內容不可將書籍刪除
//	public Boolean selectReviewByBookId(Integer bookId) {
//		Boolean exists = false;
//		try {
//			Session session = sessionFactory.getCurrentSession();
//			String hql = "SELECT count(r) FROM ReviewBean r WHERE r.bookId = :id";
//			Long count = (Long) session.createQuery(hql).setParameter("id", bookId).uniqueResult();
//
//			exists = (count > 0);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			exists = false;
//		}
//		return exists;
//	}
//
//	// delete驗證，若訂單有該書，不可將書籍刪除
//	public Boolean selectOrderItemByBookId(Integer bookId) {
//		Boolean exists = false;
//		try {
//			Session session = sessionFactory.getCurrentSession();
//
//			String hql = "SELECT count(i) FROM OrderItem i WHERE i.bookId = :id";
//
//			Long count = (Long) session.createQuery(hql).setParameter("id", bookId).uniqueResult();
//
//			exists = (count > 0);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			exists = false;
//		}
//		return exists;
//	}
//
//	public BooksBean selectBookByName(String bookName) {
//
//		return null;
//	}
//
//	// insert books---------------------------
//	public BooksBean insertBooks(BooksBean booksBean) {
//		Session session = sessionFactory.getCurrentSession();
//		BooksBean bookInsert = booksBean;
//		session.persist(booksBean);
//
//		return bookInsert;
//	}
//
//	// delete book by Id-----
//	public void deleteBooks(Integer bookId) {
//		Session session = sessionFactory.getCurrentSession();
//		BooksBean bookdelete = selectBooksById(bookId);
//		session.remove(bookdelete);
//	}
//
//	// Update Book--------------
//	public BooksBean upDateBook(BooksBean booksBean) {
//		Session session = sessionFactory.getCurrentSession();
//		BooksBean bookUpdate = (BooksBean) session.merge(booksBean);
//		return bookUpdate;
//	}
//
//	// Update on_shelf
//	public boolean updateOnShelfStatus(Integer bookId, boolean newStatus) {
//		try {
//			Session session = sessionFactory.getCurrentSession();
//
//			String hql = "UPDATE BooksBean b SET b.onShelf = :status WHERE b.bookId = :id";
//			int rowCount = session.createQuery(hql).setParameter("status", newStatus).setParameter("id", bookId)
//					.executeUpdate();
//			boolean hasUpdated = rowCount > 0;
//			return hasUpdated;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
//}
