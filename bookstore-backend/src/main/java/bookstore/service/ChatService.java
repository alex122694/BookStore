package bookstore.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import bookstore.bean.ChatMessageBean;
import bookstore.config.ChatWebSocketHandler;
import bookstore.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatMessageRepository chatRepo;
	private final ChatWebSocketHandler chatHandler;
	
	public ChatMessageBean saveMessage(ChatMessageBean msg) {
		return chatRepo.save(msg);
	}
	
	public List<Map<String, Object>> getChatList(Integer adminId) {
		return chatRepo.getAdminChatList(adminId);
	}
	
	public List<ChatMessageBean> getPureHistory(Integer adminId, Integer userId) {
		return chatRepo.findChatHistory(adminId, userId);
	}
	
	public List<ChatMessageBean> getHistory(Integer adminId, Integer userId) {
		return chatRepo.findChatHistory(adminId, userId);
	}
	
	public void notifyReadStatus(Integer receiverId) {
		chatHandler.sendToUser(receiverId, "READ_SIGNAL");
	}
}
