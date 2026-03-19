package bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.bean.BooksBean;

public interface BookRepository extends JpaRepository<BooksBean, Integer> {

	Optional<BooksBean> findByIsbn(String isbn);

	List<BooksBean> findByGenres_GenreId(Integer genreId);

	// 新增加--
	List<BooksBean> findByOnShelf(Integer onShelf);

	@org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
	@org.springframework.data.jpa.repository.Query("SELECT b FROM BooksBean b WHERE b.bookId = :id")
	Optional<BooksBean> findByIdWithLock(@org.springframework.data.repository.query.Param("id") Integer id);

}
