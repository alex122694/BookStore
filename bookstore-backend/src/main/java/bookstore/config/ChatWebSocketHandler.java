package bookstore.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
	
	private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String userId = getUserId(session);
		if(userId != null) {
			sessions.put(userId, session);
			System.out.println("用戶連線: " + userId);
		}
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessions.remove(getUserId(session));
	}
	
	public void sendToUser(Integer receiverId, Object payload) {
	    WebSocketSession session = sessions.get(String.valueOf(receiverId));
	    if (session != null && session.isOpen()) {
	        try {
	            String message;
	            if (payload instanceof String) {
	                message = (String) payload;
	            } else {
	                message = objectMapper.writeValueAsString(payload);
	            }
	            session.sendMessage(new TextMessage(message));
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	private String getUserId(WebSocketSession session) {
		String path = session.getUri().getPath();
		return path.substring(path.lastIndexOf('/') + 1);
	}

}
