package bookstore.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import bookstore.aop.BusinessException;
import bookstore.bean.Cart;
import bookstore.service.CartService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // JSP 頁面，資料改為透過 AJAX 非同步載入，改用下面vue畫面路徑，JSP舊路徑先保留。
    @GetMapping
    public String viewCartPage() {
        return "cart/cart";
    }

    // 取得購物車項目的 API
    @GetMapping("/api/items")
    @ResponseBody
    public Map<String, Object> getCartItems(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "請先登入");
            return response;
        }

        List<Cart> cartItems = cartService.getUserCart(userId);
        response.put("cartItems", cartItems);

        // 使用Steam API來計算購物車中所有「有效商品」的總金額
        int totalAmount = cartItems.stream()
                .filter(item -> isValid(item))
                .mapToInt(item -> item.getBooksBean().getPrice().intValue() * item.getQuantity())
                .sum();
        response.put("totalAmount", totalAmount);
        response.put("success", true);

        return response;
    }

    //檢查書籍是否有效
    private boolean isValid(Cart item) {
        return item.getBooksBean().getOnShelf() != null
                && item.getBooksBean().getOnShelf() == 1 // 判斷是否上架
                && item.getBooksBean().getStock() >= item.getQuantity(); //判斷購買量是否超過庫存量
    }

    @PostMapping("/api/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestParam("bookId") Integer bookId,
            @RequestParam("quantity") Integer quantity, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        Integer userId = (Integer) request.getAttribute("userId");
        
        //檢查是否有userId(使用JWT理論上不會有問題，但還是檢查一下)
        if (userId == null) {
            response.put("success", false);
            response.put("message", "請先登入");
            return response;
        }

        try {
            cartService.addToCart(userId, bookId, quantity); // 使用從請求中提取出來的 userId
            response.put("success", true);

            // 回傳更新後的購物車品項數
            List<Cart> cartItems = cartService.getUserCart(userId);
            response.put("cartCount", cartItems.size());

            response.put("message", "加入購物車成功");
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "加入購物車失敗: " + e.getMessage());
        }
        return response;
    }

    // 更新購物車商品數量
    @PostMapping("/api/update")
    @ResponseBody
    public Map<String, Object> updateQuantity(@RequestParam("cartId") Integer cartId,
            @RequestParam("quantity") Integer quantity, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        Integer userId = (Integer) request.getAttribute("userId");
        
        //檢查是否有userId(使用JWT理論上不會有問題，但還是檢查一下)
        if (userId == null) {
            response.put("success", false);
            response.put("message", "請先登入");
            return response;
        }

        try {
            Cart cart = cartService.updateQuantity(cartId, quantity);
            
            response.put("success", true);
            response.put("quantity", cart.getQuantity());
            int subtotal = cart.getBooksBean().getPrice().intValue() * cart.getQuantity();
            response.put("subtotal", subtotal);

            List<Cart> cartItems = cartService.getUserCart(userId);
            
            // 重新計算購物車商品總計
            int total = cartItems.stream()
                    .filter(item -> isValid(item)) // 檢查有效商品
                    .mapToInt(item -> item.getBooksBean().getPrice().intValue() * item.getQuantity())
                    .sum();
            response.put("totalAmount", total);

        } catch (BusinessException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    //移除購物車商品
    @PostMapping("/api/remove")
    @ResponseBody
    public Map<String, Object> remove(@RequestParam("cartId") Integer cartId, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        Integer userId = (Integer) request.getAttribute("userId");
        
        //檢查是否有userId(使用JWT理論上不會有問題，但還是檢查一下)
        if (userId == null) {
            response.put("success", false);
            return response;
        }

        try {
            cartService.removeFromCart(cartId);
            response.put("success", true);

            List<Cart> cartItems = cartService.getUserCart(userId);
           
            // 移除後重新計算商品總計
            int total = cartItems.stream()
                    .filter(item -> isValid(item)) // 檢查有效商品
                    .mapToInt(item -> item.getBooksBean().getPrice().intValue() * item.getQuantity())
                    .sum();
            response.put("totalAmount", total);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
