package bookstore.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import bookstore.aop.BusinessException;
import bookstore.bean.BookClubsBean;
import bookstore.bean.BooksBean;
import bookstore.bean.ClubCategoriesBean;
import bookstore.bean.ClubConstants;
import bookstore.bean.ClubDetail;
import bookstore.bean.ClubRegistrationsBean;
import bookstore.bean.UserBean;
import bookstore.dto.BookClubRequestDTO;
import bookstore.repository.BookClubsRepository;
import bookstore.repository.BookRepository;
import bookstore.repository.ClubCategoriesRepository;
import bookstore.repository.ClubDetailRepository;
import bookstore.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class BookClubService {

	private final EmailService emailService;

	@Autowired
	private BookClubsRepository bookClubsRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private ClubCategoriesRepository categoriesRepository;

	@Autowired
	private ClubDetailRepository clubDetailRepository;

	@Autowired
	private bookstore.repository.ClubRegistrationsRepository clubRegistrationsRepository;

	private static final String ROOT_PATH = "C:/uploads/proof/";

	BookClubService(EmailService emailService) {
		this.emailService = emailService;
	}

	// 查詢全部讀書會
	public List<BookClubsBean> getAllClubs() {
		List<BookClubsBean> clubList = bookClubsRepository.findAll();
		log.info("查詢成功，共 {} 筆資料", clubList.size());
		return clubList;
	}

	// 查詢所有讀書會分類
	public List<ClubCategoriesBean> getAllCategories() {
		return categoriesRepository.findAll();
	}

	// 查詢單筆讀書會
	public BookClubsBean getClub(Integer clubId) {
		Optional<BookClubsBean> opt = bookClubsRepository.findById(clubId);
		if (opt.isPresent()) {
			log.info("查詢成功");
			return opt.get();
		}
		log.warn("查無ID:[]讀書會資料", clubId);
		throw new BusinessException(404, "查無ID:[" + clubId + "]讀書會資料");
	}

	// 根據類型查詢讀書會
	public List<BookClubsBean> findByCategory(Integer category) {
		List<BookClubsBean> list = bookClubsRepository.findByCategoriesBean_CategoryId(category);
		if (list.isEmpty()) {
			log.warn("查無該類型相關讀書會");
			return list;
		}
		log.info("查詢成功 - 共 [] 筆資料", list.size());
		return list;
	}

	// 根據狀態查詢讀書會
	public List<BookClubsBean> findByStatus(Integer status) {
		List<BookClubsBean> list = bookClubsRepository.findByStatus(status);
		if (list.isEmpty()) {
			log.warn("查無狀態為{}的讀書會", status);
			return list;
		}
		log.info("查詢成功 - 共{}筆資料", list.size());
		return list;
	}

	// 根據書籍查詢讀書會
	public BookClubsBean findByBooks(BooksBean booksBean) {
		Optional<BookClubsBean> opt = bookClubsRepository.findByBook(booksBean);
		if (opt.isEmpty()) {
			log.warn("查無{}相關讀書會", booksBean.getBookName());
			throw new BusinessException(404, "查無該書相關讀書會");
		}
		return opt.get();
	}

	// 根據發起人(會員)查詢讀書會
	public List<BookClubsBean> findByHost(UserBean userBean) {
		List<BookClubsBean> list = bookClubsRepository.findByHost(userBean);
		if (list.isEmpty()) {
			log.warn("查無{}所發起的讀書會", userBean.getUserName());
			return list;
		}
		log.info("查詢成功 - 由{}所發起的讀書會，共{}筆", userBean.getUserName(), list.size());
		return list;
	}

	// 根據 Host ID 查詢 (Controller 專用)
	public List<BookClubsBean> findByHostId(Integer userId) {
		Optional<UserBean> userOpt = userRepository.findById(userId);
		if (userOpt.isEmpty()) {
			throw new BusinessException(404, "查無使用者");
		}
		return findByHost(userOpt.get());
	}

	// 讀書會發起，發起人等級驗證
	public void HostPointCheck(Integer userId) {
		Optional<UserBean> user = userRepository.findById(userId);
		Integer userPoint = user.get().getPoints();

	}

	// 讀書會發起欄位驗證
	private void validateFullClubDetail(BookClubRequestDTO dto) {
		if (dto.getClubName() == null || dto.getClubName().trim().isBlank()) {
			log.warn("讀書會名稱不可空白");
			throw new BusinessException(400, "讀書會名稱不可空白");
		}
		if (dto.getEventDate() == null || dto.getDeadLine() == null) {
			throw new BusinessException(400, "活動時間及截止時間不可空白");
		}

		if (dto.getEventDate().isBefore(LocalDateTime.now())) {
			throw new BusinessException(400, "活動時間不可在過去");
		}

		if (dto.getDeadLine().isAfter(dto.getEventDate())) {
			throw new BusinessException(400, "活動截止時間不設定在活動時間之後");
		}

		if (dto.getPurpose() == null || dto.getPurpose().trim().isBlank()) {
			throw new BusinessException(400, "活動宗旨不可空白");
		}
		if (dto.getAgenda() == null || dto.getAgenda().trim().isBlank()) {
			throw new BusinessException(400, "活動議程不可空白");
		}
		if (dto.getLocation() == null || dto.getLocation().trim().isBlank()) {
			throw new BusinessException(400, "活動地點不可空白");
		}

		// 難度等級驗證：巨木級(3) 必須上傳佐證資料
		if (dto.getDifficulty() != null && dto.getDifficulty() == 3) {
			if (dto.getProofPath() == null || dto.getProofPath().trim().isBlank()) {
				throw new BusinessException(400, "巨木級(專家)讀書會需上傳佐證資料");
			}
		}
	}

	// 將DTO映射到Club Entity
	private void mapDtoToMainEntity(BookClubsBean club, BookClubRequestDTO dto) {
		if (dto.getClubName() != null)
			club.setClubName(dto.getClubName());
		if (dto.getLocation() != null)
			club.setLocation(dto.getLocation());
		if (dto.getEventDate() != null)
			club.setEventDate(dto.getEventDate());
		if (dto.getDeadLine() != null)
			club.setDeadline(dto.getDeadLine());
		if (dto.getMaxParticipants() != null)
			club.setMaxParticipants(dto.getMaxParticipants());

		if (dto.getCategoryId() != null) {
			categoriesRepository.findById(dto.getCategoryId()).ifPresent(club::setCategoriesBean);
		}

		if (dto.getBookId() != null) {
			bookRepository.findById(dto.getBookId()).ifPresent(club::setBook);
		}
	}

	// 將DTO映射到Club Detail Entity
	private void mapDtoToDetailEntity(ClubDetail detail, BookClubRequestDTO dto) {
		if (dto.getPurpose() != null)
			detail.setPurpose(dto.getPurpose());
		if (dto.getAgenda() != null)
			detail.setAgenda(dto.getAgenda());
		if (dto.getModeType() != null)
			detail.setModeType(dto.getModeType());
		if (dto.getRequirement() != null)
			detail.setRequirement(dto.getRequirement());
		if (dto.getProofPath() != null)
			detail.setProofPath(dto.getProofPath());
		if (dto.getDifficulty() != null)
			detail.setDiffultLevel(dto.getDifficulty());
	}

	// 發起讀書會
	public BookClubsBean createBookClub(BookClubRequestDTO dto, Integer userId) throws IOException {

		// 身分驗證
		Optional<UserBean> host = userRepository.findById(userId);
		if (host.isEmpty()) {
			log.warn("查無ID:{}的會員資訊", userId);
			throw new BusinessException(404, "查無該會員資訊");
		}

		UserBean mainHost = host.get();

		// 積分驗證 (一般會員需 >= 500)
		int role = mainHost.getUserType() != null ? mainHost.getUserType() : ClubConstants.ROLE_MEMBER;

		if (role == ClubConstants.ROLE_MEMBER) {
			int points = mainHost.getPoints() != null ? mainHost.getPoints() : 0;
			if (points < 500) {
				throw new BusinessException(403, "您的積分不足 500，無法發起讀書會");
			}
		}

		log.info("會員ID{}，姓名:{}正在發起讀書會", userId, mainHost.getUserName());

		// 初始狀態決定
		int targetStatus;
		if ("DRAFT".equalsIgnoreCase(dto.getAction())) {
			targetStatus = ClubConstants.STATUS_DRAFT;

			if (dto.getClubName() == null || dto.getClubName().trim().isBlank()) {
				throw new BusinessException(400, "至少需填寫讀書會名稱");
			}
		} else {
			validateFullClubDetail(dto);
			targetStatus = ClubConstants.STATUS_PENDING;
		}

		BookClubsBean mainclub = new BookClubsBean();
		mainclub.setHost(mainHost);
		mainclub.setStatus(targetStatus);
		mainclub.setCreatedAt(LocalDateTime.now());

		mapDtoToMainEntity(mainclub, dto);

		BookClubsBean saveClub = bookClubsRepository.save(mainclub);

		ClubDetail detail = new ClubDetail();
		detail.setMainClub(saveClub);
		mapDtoToDetailEntity(detail, dto);
		clubDetailRepository.save(detail);
		log.info("讀書會建立成功 ID:{} ，狀態:{}", saveClub.getClubId(), saveClub.getStatus());
		return saveClub;

	}

	// 通用檔案上傳
	public String uploadFile(MultipartFile file, String folderName) throws IllegalStateException, IOException {
		if (file == null || file.isEmpty()) {
			return null;
		}

		try {
			File diretory = new File(ROOT_PATH + folderName);
			if (!diretory.exists()) {
				diretory.mkdirs();
			}

			String originalFileName = file.getOriginalFilename();
			if (originalFileName == null)
				originalFileName = "unknown";
			String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

			File destFile = new File(diretory, uniqueFileName);
			file.transferTo(destFile);
			return "/" + folderName + "/" + uniqueFileName;
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException(500, "檔案上傳失敗");
		}
	}

	// 修改讀書會第一階段 權限與狀態檢查
	public boolean ValidatePermissionAndState(BookClubsBean existing, Integer userRole, Integer userId) {
		int status = existing.getStatus();
		boolean needTest = false;
		// 會員邏輯
		if (userRole == ClubConstants.ROLE_MEMBER) {
			if (!Objects.equals(existing.getHost().getUserId(), userId)) {
				throw new BusinessException(403, "您無權修改其他人發起的讀書會");
			}

			// 狀態檢查
			if (status == ClubConstants.STATUS_APPROVED) {
				throw new BusinessException(400, "讀書會已審核通過報名中，不可修改內部資料");
			} else if (status == ClubConstants.STATUS_ENDED) {
				throw new BusinessException(400, "讀書會已結束，不可修改");
			} else if (status == ClubConstants.STATUS_CANCELLED) {
				throw new BusinessException(400, "讀書會已取消，不可修改");
			} else if (status == ClubConstants.STATUS_REJECTED) {
				needTest = true; // 駁回後可修改
			} else if (status == ClubConstants.STATUS_DRAFT) {
				// 草稿可修改，不做額外限制
			} else if (status == ClubConstants.STATUS_PENDING) {
				// 審核中通常不允許修改，或者視需求而定。依報告建議，審核中應鎖定。
				throw new BusinessException(400, "審核中狀態無法進行修改");
			} else {
				// 其他未定義狀態
				throw new BusinessException(400, "目前狀態無法進行修改");
			}

			// 管理員邏輯
		} else if (userRole == ClubConstants.ROLE_ADMIN) {
			// 管理員修改權限邏輯，暫時保持原樣或依需求放寬
			// if (status == ClubConstants.STATUS_ENDED) throw ...
		}
		return needTest;
	}

	// 修改讀書會第二階段 資料欄位正確性驗證
	public void validateFileds(BookClubsBean existing, BookClubsBean incoming) {

		// 人數上限檢查
		if (incoming.getMaxParticipants() != null) {
			int current = (existing.getMaxParticipants() == null) ? 0 : existing.getMaxParticipants();
			if (incoming.getMaxParticipants() < current) {
				throw new BusinessException(400, "修改人數上限不可低於現在報名人數");
			}
		}

		// 時間邏輯檢查
		LocalDateTime newEvenTime = incoming.getEventDate() != null ? incoming.getEventDate() : existing.getEventDate();
		LocalDateTime newDeadLine = incoming.getDeadline() != null ? incoming.getDeadline() : existing.getDeadline();

		// 截止日必須在活動開始之前
		if (newDeadLine.isAfter(newEvenTime)) {
			throw new BusinessException(400, "報名截止時間不可早於活動開始時間");
		}

		// 若修改活動時間，不能設為過去
		if (newEvenTime.isBefore(LocalDateTime.now())) {
			throw new BusinessException(400, "活動時間不可設在過去");
		}

		// 名稱檢查
		if (incoming.getClubName() != null) {
			if (incoming.getClubName().trim().isEmpty() || incoming.getClubName().length() > 100) {
				throw new BusinessException(400, "輸入格式錯誤，讀書會名稱需在100字以內");
			}
		}
	}

	// 資料搬移
	private void mergeClubData(BookClubsBean target, BookClubsBean source) {
		if (source.getClubName() != null)
			target.setClubName(source.getClubName());
		if (source.getLocation() != null)
			target.setLocation(source.getLocation());
		if (source.getExternalBookInfo() != null)
			target.setExternalBookInfo(source.getExternalBookInfo());
		if (source.getEventDate() != null)
			target.setEventDate(source.getEventDate());
		if (source.getDeadline() != null)
			target.setDeadline(source.getDeadline());
		if (source.getMaxParticipants() != null)
			target.setMaxParticipants(source.getMaxParticipants());
		if (source.getCategoriesBean() != null)
			target.setCategoriesBean(source.getCategoriesBean());
		if (source.getBook() != null)
			target.setBook(source.getBook());
	}

	// 處理狀態流轉邏輯
	private void handleStateTransition(BookClubsBean club, BookClubRequestDTO dto) {
		int currentStatus = club.getStatus();
		String action = dto.getAction();

		if ("SUBMIT".equalsIgnoreCase(action)) {
			// 若從 草稿 或 駁回 狀態送出 -> 轉為 審核中
			if (currentStatus == ClubConstants.STATUS_DRAFT || currentStatus == ClubConstants.STATUS_REJECTED) {
				validateFullClubDetail(dto); // 送審前強制檢查
				club.setStatus(ClubConstants.STATUS_PENDING);
				club.setRejectionReason(null); // 清空之前的駁回原因
				log.info("讀書會 ID: {} 狀態由 {} 轉為 審核中", club.getClubId(), currentStatus);
			}
		} else if ("DRAFT".equalsIgnoreCase(action)) {
		}
	}

	// 修改讀書會主方法
	public BookClubsBean updateBookclub(Integer clubId, BookClubRequestDTO dto, Integer currentUserId,
			Integer currentUserRole) throws IllegalStateException, IOException {

		// 讀書會存在檢查
		Optional<BookClubsBean> club = bookClubsRepository.findById(clubId);
		if (club == null) {
			throw new BusinessException(400, "查無該讀書會資料");
		}

		// 讀書會權限檢查 mmd
		BookClubsBean existingClub = club.get();
		ValidatePermissionAndState(existingClub, currentUserRole, currentUserId);

		// 處理狀態流轉
		handleStateTransition(existingClub, dto);

		// 更新資料
		// 主表更新
		mapDtoToMainEntity(existingClub, dto);

		// 更新附表
		Optional<ClubDetail> detail = clubDetailRepository.findByMainClub_ClubId(clubId);
		ClubDetail existingDetail = detail.get();

		if (existingDetail.getMainClub() == null) {
			existingDetail.setMainClub(existingClub);

		}

		mapDtoToDetailEntity(existingDetail, dto);

		return bookClubsRepository.save(existingClub);

	}

	// 管理員核准讀書會
	public BookClubsBean approveClub(Integer clubId, Integer adminId) {
		BookClubsBean club = getClub(clubId);

		// 狀態檢查，僅審核中可通過 (或視需求放寬)
		// if (club.getStatus() != ClubConstants.STATUS_PEDING) {
		// throw new BusinessException(400, "非審核中狀態，無法核准");
		// }

		// 設定為報名中 (1) 或 已核准 (1) - 假設 ClubConstants.STATUS_APPROVED = 1
		// 根據常數定義 STATUS_REGISTERing = 1
		club.setStatus(ClubConstants.STATUS_APPROVED); // 或 STATUS_REGISTERing
		club.setRejectionReason(null); // 清空駁回原因
		emailService.sendAccetToHost(club.getHost().getEmail(), club.getClubName(), club.getHost().getUserName());
		log.info("管理員ID:{} 核准了讀書會ID:{}", adminId, clubId);
		return bookClubsRepository.save(club);
	}

	// 管理員駁回讀書會
	public BookClubsBean rejectClub(Integer clubId, String reason, Integer adminId) {
		BookClubsBean club = getClub(clubId);

		if (reason == null || reason.trim().isEmpty()) {
			throw new BusinessException(400, "駁回原因不可空白");
		}

		club.setStatus(ClubConstants.STATUS_REJECTED);
		club.setRejectionReason(reason);
		emailService.sendRejectToHost(club.getHost().getEmail(), club.getClubName(),club.getHost().getUserName(), reason);
		log.info("管理員ID:{} 駁回了讀書會ID:{}，原因:{}", adminId, clubId, reason);
		return bookClubsRepository.save(club);
	}

	// 發起人結束讀書會 (並結算積分)
	public BookClubsBean endClub(Integer clubId, Integer userId) {
		BookClubsBean club = getClub(clubId);

		// 權限檢查
		if (!Objects.equals(club.getHost().getUserId(), userId)) {
			throw new BusinessException(403, "您無權結束此讀書會");
		}

		// 狀態檢查 (需為 報名中/已額滿/已截止)
		int status = club.getStatus();
		if (status != ClubConstants.STATUS_APPROVED && status != ClubConstants.STATUS_FULL
				&& status != ClubConstants.STATUS_DEADLINE) {
			throw new BusinessException(400, "目前狀態無法結束讀書會");
		}

		// 時間檢查: 必須過了活動時間才能結束
		if (club.getEventDate() != null && LocalDateTime.now().isBefore(club.getEventDate())) {
			throw new BusinessException(400, "活動尚未開始，無法結束讀書會");
		}

		club.setStatus(ClubConstants.STATUS_ENDED);
		bookClubsRepository.save(club);

		// 積分結算: 針對有效報名者 (status=1) 增加 100 積分
		List<ClubRegistrationsBean> registrations = clubRegistrationsRepository.findAllByClubId(clubId);
		List<UserBean> userPointUpdate = new ArrayList<UserBean>();
		for (ClubRegistrationsBean reg : registrations) {
			if (reg.getStatus() == 1) { // 1: 報名成功
				UserBean participant = reg.getUser();
				int currentPoints = participant.getPoints() != null ? participant.getPoints() : 0;
				participant.setPoints(currentPoints + 100);
				log.info("會員ID:{} 參加讀書會ID:{} 獲得 100 積分", participant.getUserId(), clubId);
				userPointUpdate.add(participant);
			}
		}
		if (!userPointUpdate.isEmpty()) {
			userRepository.saveAll(userPointUpdate);
		}

		log.info("讀書會ID:{} 已結束，並完成積分發放", clubId);
		return club;
	}

	// 發起人取消讀書會
	public BookClubsBean cancelClub(Integer clubId, Integer userId) {
		BookClubsBean club = getClub(clubId);
		Optional<UserBean> opt = userRepository.findById(userId);
		if (opt == null) {
			throw new BusinessException(400, "查無該會員資料");
		}
		UserBean host = opt.get();

		// 權限檢查: 必須是發起人
		if (!Objects.equals(club.getHost().getUserId(), userId)) {
			throw new BusinessException(403, "您無權取消此讀書會");
		}

		int status = club.getStatus();
		if (status == ClubConstants.STATUS_ENDED || status == ClubConstants.STATUS_CANCELLED
				|| status == ClubConstants.STATUS_REJECTED) {
			throw new BusinessException(400, "目前狀態無法取消 (已結束/已取消/已駁回)");
		}

		club.setStatus(ClubConstants.STATUS_CANCELLED);

		// 通知所有參與者讀書會取消
		List<ClubRegistrationsBean> regs = clubRegistrationsRepository.findByBookClub_ClubId(clubId);
		for (ClubRegistrationsBean reg : regs) {
			emailService.sendClubCancelToRegister(reg.getUser().getEmail(), club.getClubName(), host.getUserName(),
					club.getEventDate(), reg.getUser().getUserName(), club.getLocation(), host.getEmail(),
					host.getPhoneNum());
		}

		log.info("發起人ID:{} 取消了讀書會ID:{}", userId, clubId);
		return bookClubsRepository.save(club);
	}

	// 刪除讀書會
	public void deleteClubId(Integer clubId) {
		Optional<BookClubsBean> opt = bookClubsRepository.findById(clubId);
		if (opt.isEmpty()) {
			log.warn("刪除失敗 - 無ID{}相關讀書會資料", clubId);
			throw new BusinessException(400, "刪除失敗 - 無ID" + clubId + "相關讀書會資料");
		}
		List<ClubRegistrationsBean> regs = clubRegistrationsRepository.findByBookClub_ClubId(clubId);
		for (ClubRegistrationsBean reg : regs) {
			clubRegistrationsRepository.delete(reg);
		}

		bookClubsRepository.deleteById(clubId);
	}

}
