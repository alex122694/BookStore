package com.bookstore.book;


import org.springframework.data.jpa.repository.JpaRepository;

import com.bookstore.book.BookImageBean;

public interface BookImageRepository extends JpaRepository<BookImageBean, Integer> {

}
