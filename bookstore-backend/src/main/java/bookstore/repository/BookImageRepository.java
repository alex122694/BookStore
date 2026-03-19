package bookstore.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.bean.BookImageBean;

public interface BookImageRepository extends JpaRepository<BookImageBean, Integer> {

}
