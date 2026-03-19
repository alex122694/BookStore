package bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.bean.GenreBean;

public interface GenreRepository extends JpaRepository<GenreBean, Integer> {

}
