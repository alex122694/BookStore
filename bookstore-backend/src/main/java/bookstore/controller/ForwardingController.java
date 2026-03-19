package bookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA Forwarding Controller
 * 將前端路由請求轉導至 index.html，交由 Vue Router 處理。
 */
@Controller
public class ForwardingController {

    @RequestMapping(value = {
            "/",
            "/home",
            "/dev/**",
            "/books/**",
            "/cart/**",
            "/login",
            "/register",
            "/user/**",
            "/admin/**",
            "/search"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
