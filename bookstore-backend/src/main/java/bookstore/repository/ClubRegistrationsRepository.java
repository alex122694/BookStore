package bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import bookstore.bean.ClubRegistrationsBean;

public interface ClubRegistrationsRepository extends JpaRepository<ClubRegistrationsBean, Integer> {

	List<ClubRegistrationsBean> findByBookClub_ClubId(Integer clubId);

	List<ClubRegistrationsBean> findByUser_UserId(Integer userId);

	Optional<ClubRegistrationsBean> findByBookClub_ClubIdAndUser_UserId(Integer clubId, Integer userId);

	@org.springframework.data.jpa.repository.Query("SELECT r FROM ClubRegistrationsBean r JOIN FETCH r.user WHERE r.bookClub.clubId = :clubId")
	List<ClubRegistrationsBean> findAllByClubId(
			@org.springframework.data.repository.query.Param("clubId") Integer clubId);

	boolean existsByBookClub_ClubIdAndUser_UserId(Integer clubId, Integer userId);

}
