package bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.bean.LogItemBean;

public interface LogItemRepository extends JpaRepository<LogItemBean, Integer> {

}
