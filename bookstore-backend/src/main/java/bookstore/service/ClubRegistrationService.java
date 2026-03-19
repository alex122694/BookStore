package bookstore.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bookstore.aop.BusinessException;
import bookstore.bean.BookClubsBean;
import bookstore.bean.ClubRegistrationsBean;
import bookstore.bean.UserBean;
import bookstore.repository.BookClubsRepository;
import bookstore.repository.ClubRegistrationsRepository;
import bookstore.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClubRegistrationService {
	@Autowired
	private ClubRegistrationsRepository clubRegistrationsRepository;

	@Autowired
	private BookClubsRepository bookClubsRepository;

	@Autowired
	private UserRepository userRepository;

	private boolean clubCheck(Integer clubId) {
		Optional<BookClubsBean> opt = bookClubsRepository.findById(clubId);
		if (opt == null) {
			return false;
		}
		return true;
	}

	// 找全部報名表
	public List<ClubRegistrationsBean> getAllRegistrations() {
		return clubRegistrationsRepository.findAll();
	}

	// 根據ID找報名表
	public ClubRegistrationsBean getRegistrationById(Integer regId) {
		Optional<ClubRegistrationsBean> reg = clubRegistrationsRepository.findById(regId);
		if (regId != null) {
			return reg.get();
		}
		throw new BusinessException(404, "找不到ID" + regId + "相關報名表資料");
	}

	// 根據讀書會找全部報名表
	public List<ClubRegistrationsBean> getRegistrationsByClubId(Integer clubId) {
		List<ClubRegistrationsBean> regList = clubRegistrationsRepository.findAllByClubId(clubId);
		log.info("查詢成功 - 讀書會ID:{} 共有{}份報名表", clubId, regList.size());
		return regList;
	}

	// 找User參與的所有讀書會
	public List<ClubRegistrationsBean> getRegistrationByUserId(Integer userId) {
		List<ClubRegistrationsBean> regList = clubRegistrationsRepository.findByUser_UserId(userId);
		log.info("查詢成功 - 會員ID:{} 共報名參加{}場讀書會", userId, regList.size());
		return regList;
	}

	// 針對club申請參加
	public ClubRegistrationsBean register(Integer clubId, Integer userId) {
		Optional<BookClubsBean> validClub = bookClubsRepository.findById(clubId);
		if (validClub.isEmpty()) {
			throw new BusinessException(404, "讀書會 ID:" + clubId + " 不存在");
		}

		BookClubsBean club = validClub.get();
		if (club.getStatus() != 1) {
			throw new BusinessException(400, "該讀書會目前未開放報名或已結束");
		}

		// 檢查是否為發起人 (不可報名自己的讀書會)
		if (club.getHost() != null && club.getHost().getUserId().equals(userId)) {
			throw new BusinessException(400, "發起人不可報名自己的讀書會");
		}

		// 檢查是否已報名
		Optional<ClubRegistrationsBean> existOpt = clubRegistrationsRepository
				.findByBookClub_ClubIdAndUser_UserId(clubId, userId);

		if (existOpt.isPresent()) {
			ClubRegistrationsBean exist = existOpt.get();
			// 若是已取消(status=2)，允許重新報名
			if (exist.getStatus() == 2) {
				exist.setStatus(1); // 1: 報名成功
				exist.setCheckIn(false);
				exist.setRegisteredAt(java.time.LocalDateTime.now());
				updateClubParticipants(club, 1);
				return clubRegistrationsRepository.save(exist);
			}
			throw new BusinessException(409, "您已經報名過此讀書會");
		}

		// 檢查人數上限
		if (club.getMaxParticipants() != null &&
				club.getCurrentParticipants() != null &&
				club.getCurrentParticipants() >= club.getMaxParticipants()) {
			throw new BusinessException(400, "報名人數已達上限");
		}

		UserBean user = userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(404, "使用者不存在"));

		ClubRegistrationsBean newReg = new ClubRegistrationsBean();
		newReg.setBookClub(club);
		newReg.setUser(user);
		newReg.setStatus(1); // 1: 報名成功
		newReg.setCheckIn(false);
		newReg.setRegisteredAt(java.time.LocalDateTime.now());

		try {
			updateClubParticipants(club, 1);
			return clubRegistrationsRepository.save(newReg);
		} catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
			throw new BusinessException(409, "報名人數更動頻繁，請稍後重試");
		}
	}

	private void updateClubParticipants(BookClubsBean club, int delta) {
		int current = club.getCurrentParticipants() == null ? 0 : club.getCurrentParticipants();
		club.setCurrentParticipants(current + delta);
		bookClubsRepository.saveAndFlush(club);
	}

	// 棄用: 用 register 代替
	public ClubRegistrationsBean createRegistration(Integer clubId, ClubRegistrationsBean reg) {
		return null;
	}

	// 發起人修改報名者報到狀態
	public void updateUserCheckIn(Integer clubId, Integer userId) {
		Optional<BookClubsBean> opt = bookClubsRepository.findById(clubId);
		if (opt == null) {
			throw new BusinessException(400, "查無該讀書會資料");
		}
		BookClubsBean club = opt.get();

		ClubRegistrationsBean reg = clubRegistrationsRepository
				.findByBookClub_ClubIdAndUser_UserId(clubId, userId)
				.orElseThrow(() -> new BusinessException(404, "找不到該報名資料"));

		if (Boolean.TRUE.equals(reg.getCheckIn())) {
			throw new BusinessException(400, "該會員已報到，不可重複修改");
		}
		if (LocalDateTime.now().isBefore(club.getEventDate())) {
			throw new BusinessException(409, "活動開始前不開放報到");
		}
		reg.setCheckIn(true);
		clubRegistrationsRepository.save(reg);
	}

	// 報名者取消讀書會報名
	public void updateCancel(Integer clubId, Integer userId) {
		ClubRegistrationsBean reg = clubRegistrationsRepository
				.findByBookClub_ClubIdAndUser_UserId(clubId, userId)
				.orElseThrow(() -> new BusinessException(404, "找不到該報名資料"));

		if (reg.getStatus() == 2) {
			throw new BusinessException(400, "已取消，請勿重複操作");
		}

		reg.setStatus(2); // 2: 已取消
		clubRegistrationsRepository.save(reg);

		// 扣減人數
		BookClubsBean club = reg.getBookClub();
		updateClubParticipants(club, -1);
	}
}
