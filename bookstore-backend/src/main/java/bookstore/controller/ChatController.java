package bookstore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import bookstore.bean.ChatMessageBean;
import bookstore.config.ChatWebSocketHandler;
import bookstore.repository.ChatMessageRepository;
import bookstore.service.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("api/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;
	private final ChatWebSocketHandler chatHandler;
	private final ChatMessageRepository chatRepo;
	
	@GetMapping("/admin/list")
	@ResponseBody
	public ResponseEntity<?> getChatList(HttpSession session) {
		return ResponseEntity.ok(chatService.getChatList(1));
	}
	
	@GetMapping("/history/{userId}")
	@ResponseBody
	public ResponseEntity<?> getHistory(@PathVariable Integer userId) {
		return ResponseEntity.ok(chatService.getHistory(1, userId));
	}
	
	@PostMapping("/send")
	@ResponseBody
	public ResponseEntity<?> send(@RequestBody ChatMessageBean msg) {
		System.out.println("發送者ID: " + msg.getSenderId() + " -> 接收者ID: " + msg.getReceiverId());
		
		ChatMessageBean savedMsg = chatService.saveMessage(msg);
		chatHandler.sendToUser(savedMsg.getReceiverId(), savedMsg);
		
		return ResponseEntity.ok(savedMsg);
	}
	
	@GetMapping("/markRead/{targetId}/{readerId}")
	@ResponseBody
	@Transactional
	public void markRead(@PathVariable Integer targetId, @PathVariable Integer readerId) {
	    
	    chatRepo.markAsRead(targetId, readerId); 
	    
	    chatHandler.sendToUser(targetId, "READ_SIGNAL");
	}
	
	@GetMapping("/unread/{userId}")
	@ResponseBody
	public ResponseEntity<Integer> getUnreadCount(@PathVariable Integer userId) {
		Integer count = chatRepo.countUnreadMessages(1, userId);
		return ResponseEntity.ok(count);
	}
	
	@GetMapping("/admin/unread-users")
	@ResponseBody
	public ResponseEntity<Long> getUnreadUserCount() {
		long count = chatRepo.countUnreadUsers(1);
		return ResponseEntity.ok(count);
	}
	
}
