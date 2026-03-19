package bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.bean.BookClubsBean;
import bookstore.bean.ClubCategoriesBean;

public interface ClubCategoriesRepository extends JpaRepository<ClubCategoriesBean	, Integer> {


}
