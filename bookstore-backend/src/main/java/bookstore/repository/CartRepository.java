package bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookstore.bean.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    //取得用戶購物車資料(用以載入用戶查看購物車商品內容0
	List<Cart> findByUserId(Integer userId);

	//取得用戶的某筆購物車明細(用以比對用戶將商品加入購物車時，比對該商品於購物車的數量是否超過目前庫存量)
    Optional<Cart> findByUserIdAndBookId(Integer userId, Integer bookId);

    void deleteByUserId(Integer userId);
}
