package bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import bookstore.bean.WishlistBean;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistBean, Integer> {
	
	Optional<WishlistBean> findByUserUserIdAndBookBookId(Integer userId, Integer bookId);
	
	@EntityGraph(attributePaths = {"book", "book.bookImageBean"})
	List<WishlistBean> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
	
	@Modifying
	@Transactional
	void deleteByUserUserIdAndBookBookId(Integer userId, Integer bookId);
}
