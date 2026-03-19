package bookstore.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.bean.BookClubsBean;
import bookstore.bean.BooksBean;
import bookstore.bean.UserBean;

public interface BookClubsRepository extends JpaRepository<BookClubsBean, Integer> {

	List<BookClubsBean> findByStatus(Integer status);

	Optional<BookClubsBean> findByBook(BooksBean book);

	List<BookClubsBean> findByCurrentParticipants(Integer currentParticipants);

	List<BookClubsBean> findByCategoriesBean_CategoryId(Integer categoryId);

	List<BookClubsBean> findByHost(UserBean host);
	
//	long countByHostIdAndStatusIn(Integer userId, Collection<Integer> statuses);
}
