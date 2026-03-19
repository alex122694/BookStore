package com.bookstore.book;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookstore.book.GenreBean;

public interface GenreRepository extends JpaRepository<GenreBean, Integer> {

}
