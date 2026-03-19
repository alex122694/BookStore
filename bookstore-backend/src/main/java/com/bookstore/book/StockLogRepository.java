package com.bookstore.book;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookstore.book.StockLogBean;

public interface StockLogRepository extends JpaRepository<StockLogBean, Integer> {

}
