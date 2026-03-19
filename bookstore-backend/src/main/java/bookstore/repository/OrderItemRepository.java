package bookstore.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import bookstore.bean.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    // 1. 依訂單ID查詢所有明細
    List<OrderItem> findByOrders_OrderId(Integer orderId);

    // 2. 依訂單ID刪除所有明細
    @Modifying //需要告訴程式這不是查詢語法，因為使用Query，如果不加這個annotation，程式會執行查詢然後異常
    @Transactional 
    @Query("DELETE FROM OrderItem i WHERE i.orders.orderId = :orderId")
    void deleteByOrderId(@Param("orderId") Integer orderId);
}

