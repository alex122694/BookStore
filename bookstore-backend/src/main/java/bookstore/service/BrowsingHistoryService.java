package bookstore.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bookstore.bean.BooksBean;
import bookstore.bean.BrowsingHistoryBean;
import bookstore.bean.UserBean;
import bookstore.repository.BrowsingHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BrowsingHistoryService {
	
	private final BrowsingHistoryRepository browsingRepo;
	
	// 紀錄瀏覽行為
	
	public void recordBrowsing(Integer userId, Integer bookId) {
		Optional<BrowsingHistoryBean> existingHistory = browsingRepo.findByUserUserIdAndBookBookId(userId, bookId);
		
		if (existingHistory.isPresent()) {
			BrowsingHistoryBean history = existingHistory.get();
			history.setBrowsedAt(new Date());
			browsingRepo.save(history);
		} else {
			BrowsingHistoryBean newHistory = BrowsingHistoryBean.builder()
					.user(UserBean.builder().userId(userId).build())
					.book(new BooksBean())
					.browsedAt(new Date())
					.build();
			newHistory.getBook().setBookId(bookId);
			browsingRepo.save(newHistory);
		}
	}
	
	// 取得使用者的所有瀏覽紀錄
	
	public List<BrowsingHistoryBean> getHistoryByUser(Integer userId) {
		return browsingRepo.findByUserUserIdOrderByBrowsedAtDesc(userId);
	}
	
	// 清空特定使用者的瀏覽紀錄
	
	public void clearAllHistory(Integer userId) {
		List<BrowsingHistoryBean> list = browsingRepo.findByUserUserIdOrderByBrowsedAtDesc(userId);
		browsingRepo.deleteAll(list);
	}

}
