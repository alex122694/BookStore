package bookstore.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import bookstore.bean.ChatMessageBean;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageBean, Integer> {
	@Query("SELECT c FROM ChatMessageBean c WHERE " +
	           "(c.senderId = :adminId AND c.receiverId = :userId) OR " +
	           "(c.senderId = :userId AND c.receiverId = :adminId) " +
	           "ORDER BY c.createdAt ASC")
	List<ChatMessageBean> findChatHistory(@Param("adminId") Integer adminId, @Param("userId") Integer userId);
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query("UPDATE ChatMessageBean c SET c.isRead = true " +
	       "WHERE c.senderId = :senderId AND c.receiverId = :receiverId AND c.isRead = false")
	void markAsRead(@Param("senderId") Integer senderId, @Param("receiverId") Integer receiverId);
	
	@Query(value = "SELECT u.user_id as userId, u.user_name as userName, u.img as userImg, " +
	           "m.content as lastMsg, m.createdAt as lastTime, " +
	           "(SELECT COUNT(*) FROM chat_message WHERE senderId = u.user_id AND receiverId = :adminId AND isRead = 0) as unreadCount " +
	           "FROM users u " +
	           "CROSS APPLY (SELECT TOP 1 content, createdAt FROM chat_message " +
	           "             WHERE (senderId = u.user_id AND receiverId = :adminId) OR (senderId = :adminId AND receiverId = u.user_id) " +
	           "             ORDER BY createdAt DESC) m " +
	           "ORDER BY m.createdAt DESC", nativeQuery = true)
	List<Map<String, Object>> getAdminChatList(@Param("adminId") Integer adminId);
	
	@Query("SELECT COUNT(c) FROM ChatMessageBean c WHERE c.senderId = :adminId AND c.receiverId = :userId AND c.isRead = false")
	Integer countUnreadMessages(@Param("adminId") Integer adminId, @Param("userId") Integer userId);
	
	@Query("SELECT COUNT(DISTINCT c.senderId) FROM ChatMessageBean c " +
		       "WHERE c.receiverId = :adminId AND c.isRead = false")
		long countUnreadUsers(@Param("adminId") Integer adminId);

}
