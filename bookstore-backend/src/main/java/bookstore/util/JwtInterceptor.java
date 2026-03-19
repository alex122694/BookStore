package bookstore.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.getClaims(token);
                // Role check logic...
                String role = (String) claims.get("role");
                String uri = request.getRequestURI();

                if (uri.contains("/api/data/admin-only")) {
                    if (!"SUPER_ADMIN".equals(role)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "權限不足");
                        return false;
                    }
                }

                if (uri.contains("/api/data/")) {
                    if ("SUPER_ADMIN".equals(role) || "ADMIN".equals(role)) {
                        request.setAttribute("userId", Integer.parseInt(jwtUtil.getMemberId(token))); // Ensure userId
                                                                                                      // is set for
                                                                                                      // admin APIs too
                                                                                                      // if needed
                        return true;
                    } else {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "您無權操作後台 API");
                        return false;
                    }
                }

                String userIdStr = jwtUtil.getMemberId(token);
                if (userIdStr != null) {
                    request.setAttribute("userId", Integer.parseInt(userIdStr));
                }

                return true;
            } catch (Exception e) {
                System.out.println("Token 驗證失敗: " + e.getMessage());
            }
        }

        // Fallback: Check for Session (Legacy Login support)
        // This allows JSP-logged-in users to access Vue APIs if they share the session
        // cookie
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            bookstore.bean.UserBean user = (bookstore.bean.UserBean) session.getAttribute("user");
            if (user != null) {
                System.out.println("JwtInterceptor: Found legacy session user " + user.getUserId());
                request.setAttribute("userId", user.getUserId());
                return true;
            } else {
                System.out.println("JwtInterceptor: Session exists but 'user' attribute is null");
            }
        } else {
            System.out.println("JwtInterceptor: No session found (JSESSIONID missing or invalid)");
        }

        System.out.println("JwtInterceptor: Authorization failed for URI: " + request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}