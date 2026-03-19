package bookstore.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import bookstore.bean.Orders;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {

    // 1. 依使用者ID查詢訂單 (Spring根據方法名UserId推導 SQL)
    List<Orders> findByUserBean_UserId(Integer userId);

    // 2. 查詢未被取消的訂單
    @Query("SELECT o FROM Orders o WHERE o.orderStatus NOT IN ('已取消', '已退款') OR o.orderStatus IS NULL")
    List<Orders> findActiveOrders();

    // 3. 僅查詢已取消、已退款的訂單
    @Query("SELECT o FROM Orders o WHERE o.orderStatus IN ('已取消', '已退款')")
    List<Orders> findCancelAndRefundedOrders();
    
    // 4. 根據付款狀態查詢訂單
    List<Orders> findByPaymentStatus(String paymentStatus);
}