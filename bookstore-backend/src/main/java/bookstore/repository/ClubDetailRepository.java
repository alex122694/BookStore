package bookstore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.bean.ClubDetail;
import java.util.List;


public interface ClubDetailRepository extends JpaRepository<ClubDetail	, Integer> {

	Optional<ClubDetail>  findByMainClub_ClubId(Integer clubId);
}
