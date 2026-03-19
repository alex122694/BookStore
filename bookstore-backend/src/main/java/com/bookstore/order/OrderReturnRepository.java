package com.bookstore.order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookstore.order.OrderReturnBean;

@Repository
public interface OrderReturnRepository extends JpaRepository<OrderReturnBean, Integer> {

    Optional<OrderReturnBean> findByOrders_OrderId(Integer orderId);

}
