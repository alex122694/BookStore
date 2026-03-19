package bookstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bookstore.util.JwtUtil;

@RestController
public class TestTokenController {

	@Autowired
    private JwtUtil jwtUtil;

    // 瀏覽器輸入: http://localhost:8080/get-test-token?id=1
    @GetMapping("/get-test-token")
    public String getTestToken(@RequestParam String id) {
        // 根據您的 JwtUtil 產生成員 ID 為 1, 角色為 USER 的 Token
        String token = jwtUtil.generateToken(id, "USER");
        return "Bearer " + token;
    }
}
