package com.bookstore.book;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookstore.book.LogItemBean;

public interface LogItemRepository extends JpaRepository<LogItemBean, Integer> {

}
