package com.bookstore.bookclub;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookstore.bookclub.BookClubsBean;
import com.bookstore.bookclub.ClubCategoriesBean;

public interface ClubCategoriesRepository extends JpaRepository<ClubCategoriesBean	, Integer> {


}
