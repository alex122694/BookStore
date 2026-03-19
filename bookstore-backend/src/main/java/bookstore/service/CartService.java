package bookstore.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bookstore.aop.BusinessException;
import bookstore.bean.BooksBean;
import bookstore.bean.Cart;
import bookstore.repository.BookRepository;
import bookstore.repository.CartRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BookRepository bookRepository;

    // 商品加入購物車
    public Cart addToCart(Integer userId, Integer bookId, Integer quantity) {
        BooksBean book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(400, "找不到該書籍"));

        // 判斷存貨量與上下架(null值存貨為0、書籍為下架)
        int stock = (book.getStock() != null) ? book.getStock() : 0;
        int onShelf = (book.getOnShelf() != null) ? book.getOnShelf() : 0;

        // 如果書未上架，拋出異常
        if (onShelf != 1) {
            throw new BusinessException(400, "該書籍目前未上架");
        }

        if (quantity > stock) {
            throw new BusinessException(400, "庫存不足");
        }

        Optional<Cart> existingCart = cartRepository.findByUserIdAndBookId(userId, bookId);

        Cart cart;
        if (existingCart.isPresent()) {
            cart = existingCart.get();
            int newQuantity = cart.getQuantity() + quantity;
            if (newQuantity > stock) {
                throw new BusinessException(400,
                        "加入後數量將超過庫存，目前購物車已有 " + cart.getQuantity() + " 本，庫存僅剩 " + stock + " 本");
            }
            cart.setQuantity(newQuantity);
            cart.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        } else {
            cart = new Cart(userId, bookId, quantity);
            cart.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            cart.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        }

        return cartRepository.save(cart);
    }

    // 修改購物車內個別商品數量
    public Cart updateQuantity(Integer cartId, Integer quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new BusinessException(400, "找不到該購物車項目"));

        BooksBean book = bookRepository.findById(cart.getBookId())
                .orElseThrow(() -> new BusinessException(400, "找不到該書籍"));

        int stock = (book.getStock() != null) ? book.getStock() : 0;

        if (quantity > stock) {
            throw new BusinessException(400, "庫存不足，僅剩 " + stock + " 本");
        }

        if (quantity <= 0) {
            throw new BusinessException(400, "數量必須大於 0");
        }

        cart.setQuantity(quantity);
        cart.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return cartRepository.save(cart);
    }

    // 將商品從購物車移除
    public void removeFromCart(Integer cartId) {
        cartRepository.deleteById(cartId);
    }

    // 取得用戶購物車明細資料(並且要將當前購物車資料修正到與書籍資料表資料一致---上架狀態檢查、庫存量檢查)
    public List<Cart> getUserCart(Integer userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);

        // 如果數量超過庫存，自動調整
        for (Cart item : cartItems) {
            BooksBean book = item.getBooksBean();
            if (book != null) {
                // 檢查上架狀態
                Integer onShelf = (book.getOnShelf() != null) ? book.getOnShelf() : 0;
                boolean isStatusChanged = false;

                // 如果商品未上架，且購物車目前登記為上架狀態，要將購物車狀態改為未上架
                if (onShelf != 1) {
                    if (!"OFF_SHELF".equals(item.getCartStatus())) {
                        item.setCartStatus("OFF_SHELF");
                        isStatusChanged = true;
                    }
                } else {
                    // 如果書籍重新上架，清空狀態或設為有效/空值
                    if ("OFF_SHELF".equals(item.getCartStatus())) {
                        item.setCartStatus(null); // 這裡設為 null 代表狀態正常
                        isStatusChanged = true;
                    }
                }

                int stock = (book.getStock() != null) ? book.getStock() : 0;
                if (item.getQuantity() > stock) {
                    if (stock > 0) {
                        item.setQuantity(stock);
                        isStatusChanged = true;
                    } else {
                        item.setQuantity(0); // 若無庫存則設為 0
                        isStatusChanged = true;
                    }
                }

                if (isStatusChanged) {
                    item.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                    cartRepository.save(item);
                }
            }
        }

        return cartItems;
    }

    public void clearCart(Integer userId) {
        cartRepository.deleteByUserId(userId);
    }
}
