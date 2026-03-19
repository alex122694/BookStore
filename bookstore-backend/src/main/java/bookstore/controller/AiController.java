package bookstore.controller;

import bookstore.dto.ChatRequest;
import bookstore.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        String response = aiService.callGemini(request.getMessage(), request.getBooks());
        return ResponseEntity.ok(response);
    }
}
