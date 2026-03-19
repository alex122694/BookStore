package bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookstore.bean.BrowsingHistoryBean;

@Repository
public interface BrowsingHistoryRepository extends JpaRepository<BrowsingHistoryBean, Integer> {
	
	Optional<BrowsingHistoryBean> findByUserUserIdAndBookBookId(Integer userId, Integer bookId);
	List<BrowsingHistoryBean> findByUserUserIdOrderByBrowsedAtDesc(Integer userId);
}
