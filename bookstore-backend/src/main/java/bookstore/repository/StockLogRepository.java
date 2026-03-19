package bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.bean.StockLogBean;

public interface StockLogRepository extends JpaRepository<StockLogBean, Integer> {

}
