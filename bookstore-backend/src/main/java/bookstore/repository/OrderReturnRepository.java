package bookstore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookstore.bean.OrderReturnBean;

@Repository
public interface OrderReturnRepository extends JpaRepository<OrderReturnBean, Integer> {

    Optional<OrderReturnBean> findByOrders_OrderId(Integer orderId);

}
