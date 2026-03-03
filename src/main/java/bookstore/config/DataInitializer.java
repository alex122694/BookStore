package bookstore.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;

import bookstore.bean.BookClubsBean;
import bookstore.bean.BooksBean;
import bookstore.bean.ClubCategoriesBean;
import bookstore.bean.ClubDetail;
import bookstore.bean.ClubRegistrationsBean;
import bookstore.bean.CouponBean;
import bookstore.bean.OrderItem;
import bookstore.bean.OrderReturnBean;
import bookstore.bean.Orders;
import bookstore.bean.ReviewBean;
import bookstore.bean.UserBean;
import bookstore.bean.UserCouponBean;
import bookstore.repository.BookClubsRepository;
import bookstore.repository.BookRepository;
import bookstore.repository.BrowsingHistoryRepository;
import bookstore.repository.CartRepository;
import bookstore.repository.ClubCategoriesRepository;
import bookstore.repository.ClubDetailRepository;
import bookstore.repository.ClubRegistrationsRepository;
import bookstore.repository.CouponRepository;
import bookstore.repository.OrderItemRepository;
import bookstore.repository.OrderReturnRepository;
import bookstore.repository.OrdersRepository;
import bookstore.repository.ReviewRepository;
import bookstore.repository.UserCouponRepository;
import bookstore.repository.UserLogRepository;
import bookstore.repository.UserRepository;
import bookstore.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

	private final UserRepository userRepository;
	private final BookRepository bookRepository;
	private final OrdersRepository ordersRepository;
	private final OrderItemRepository orderItemRepository;
	private final ReviewRepository reviewRepository;
	private final BookClubsRepository bookClubsRepository;
	private final ClubRegistrationsRepository clubRegistrationsRepository;
	private final ClubCategoriesRepository clubCategoriesRepository;
	private final ClubDetailRepository clubDetailRepository;
	private final PasswordEncoder passwordEncoder;
	private final BrowsingHistoryRepository browsingHistoryRepository;
	private final UserLogRepository userLogRepository;
	private final CartRepository cartRepository;
	private final CouponRepository couponRepository;
	private final WishlistRepository wishlistRepository;
	private final OrderReturnRepository orderReturnRepository;
	private final UserCouponRepository userCouponRepository;
	private final DataSource dataSource;

	private final Random random = new Random();

	@Bean
	public CommandLineRunner initData() {
		return args -> {
			log.info("=== 系統啟動：開始資料初始化流程 ===");

			// 1. 檢查會員資料 (Demo 核心 - 使用 Java 建立以支援密碼加密)
			long userCount = userRepository.count();
			if (userCount == 0) {
				log.info("偵測到無會員資料，開始產生初始管理員與會員...");
				createUsers();
			} else {
				log.info("資料庫已有 {} 名會員，略過會員初始化。", userCount);
			}

			// 2. 檢查書籍資料 (若無書籍，則手動執行 data.sql 載入書籍、訂單等其餘資料)
			long bookCount = bookRepository.count();
			if (bookCount == 0) {
				log.info("偵測到無書籍資料，開始執行 data.sql...");
				try {
					ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
					populator.setSqlScriptEncoding("UTF-8");
					populator.addScript(new ClassPathResource("data.sql"));
					populator.execute(dataSource);
					log.info("data.sql 執行完成。");
				} catch (Exception e) {
					log.error("執行 data.sql 失敗: {}", e.getMessage());
				}
			} else {
				log.info("偵測到資料庫已有 {} 本書籍。", bookCount);
			}

			// 3. 檢查分類資料
			if (clubCategoriesRepository.count() == 0) {
				createClubCategories();
			}

			

			log.info("=== 資料初始化完成 ===");
			log.info("點擊 http://localhost:8080/ 登入使用以下帳號：TreeorTree@bookstore.com / 12345  ");
		};
	}

	// 刪除現有資料
	@Transactional
	public void clearTransientData() {
		orderReturnRepository.deleteAllInBatch();
		userCouponRepository.deleteAllInBatch();
		cartRepository.deleteAllInBatch();
		wishlistRepository.deleteAllInBatch();
		couponRepository.deleteAllInBatch();
		orderItemRepository.deleteAllInBatch();
		reviewRepository.deleteAllInBatch();
		clubRegistrationsRepository.deleteAllInBatch();
		clubDetailRepository.deleteAllInBatch();
		browsingHistoryRepository.deleteAllInBatch();

		bookClubsRepository.deleteAllInBatch();
		ordersRepository.deleteAllInBatch();

		userRepository.flush();

		userLogRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();

		log.info("已清除：訂單、評價、讀書會、會員資料");
	}

	@Transactional
	public void createAdmin() {

		// UserBean superAdmin = new UserBean();
		// superAdmin.setEmail("pen@bookstore.com");
		// superAdmin.setUserName("林木森");
		// superAdmin.setUserPwd(passwordEncoder.encode("123456"));
		// superAdmin.setUserType(0);
		// superAdmin.setStatus(1);
		// superAdmin.setPoints(9999);
		// superAdmin.setCreatedAt(new Date());
		// userRepository.save(superAdmin);
		// log.info("已建立初始管理員 (僅供新增書籍用)");

	}

	// 會員初始資料
	@Transactional
	public List<UserBean> createUsers() {
		List<UserBean> users = new ArrayList<>();

		// SuperAdmin
		UserBean superAdmin = new UserBean();
		superAdmin.setEmail("TreeorTree@bookstore.com");
		superAdmin.setUserName("林木森");
		superAdmin.setUserPwd(passwordEncoder.encode("12345"));
		superAdmin.setUserType(0);
		superAdmin.setGender("M");
		superAdmin.setPhoneNum("0987654321");
		superAdmin.setStatus(1);
		superAdmin.setPoints(0);
		superAdmin.setCreatedAt(new Date());
		userRepository.save(superAdmin);
		users.add(superAdmin);

		// Admin
		UserBean admin = new UserBean();
		admin.setEmail("Maple@bookstore.com");
		admin.setUserName("林木楓");
		admin.setUserPwd(passwordEncoder.encode("12345"));
		admin.setUserType(1);
		admin.setStatus(1);
		admin.setPhoneNum("0912345678");
		admin.setAddress("桃園市中壢區中華路999號");
		admin.setPoints(0);
		admin.setCreatedAt(new Date());
		users.add(userRepository.save(admin));

		// Members
		String[] name = { "王曉明", "李鐵柱", "王翠花", "林志玲", "張大寶", "陳汁瀚", "周餅倫", "王小陸", "范承仁", "李建輝" };
		String[] phone = { "0987654321", "0987454135", "0974745241", "0954123547", "0985412534", "0958745666",
				"0965845333", "0954254855", "0985412578", "0985412544" };
		for (int i = 1; i <= 9; i++) {
			UserBean user = new UserBean();
			user.setEmail("user" + i + "@yahoo.com");
			user.setUserName(name[i]);
			user.setUserPwd(passwordEncoder.encode("123456"));
			user.setUserType(2);
			user.setGender("M");
			user.setPhoneNum(phone[i]);
			user.setStatus(1);
			user.setPoints(random.nextInt(2000));
			user.setAddress("桃園市中壢區新生路99" + i + "號");
			user.setCreatedAt(new Date());
			users.add(userRepository.save(user));
		}
		return users;
	}

	// 讀書會類別初始資料
	@Transactional
	public void createClubCategories() {
		String[] names = { "心靈健康", "投資理財", "專題講座", "作者見面會", "醫學蓋論", "程式設計", "工業管理" };
		for (String name : names) {
			ClubCategoriesBean c = new ClubCategoriesBean();
			c.setCategoryName(name);
			clubCategoriesRepository.save(c);
		}
	}

	
	// 讀書會初始資料
	@Transactional
	public void createBookClubs(int count, List<UserBean> users, List<BooksBean> books, List<ClubCategoriesBean> cats) {
		if (books.isEmpty() || cats.isEmpty())
			return;

		BookClubsBean club1 = new BookClubsBean();
		UserBean host1 = userRepository.findByEmail("pen@bookstore.com");
		club1.setClubName("書友共讀:探討投資的真諦");
		club1.setHost(host1);
		club1.setCategoriesBean(cats.get(2));
		club1.setBook(books.get(3));
		club1.setStatus(4);
		club1.setMaxParticipants(30);
		club1.setCurrentParticipants(1);
		club1.setEventDate(LocalDateTime.now().plusDays(-1));
		club1.setDeadline(LocalDateTime.now().plusDays(-5));
		club1.setLocation("聖德基督學院203教室");
		bookClubsRepository.save(club1);
		ClubDetail detail2 = new ClubDetail();
		detail2.setMainClub(club1);
		detail2.setPurpose("探討金流背後的核心邏輯");
		detail2.setAgenda("導讀 -> 分組 -> 總結");
		detail2.setDiffultLevel(2);
		clubDetailRepository.save(detail2);

		ClubRegistrationsBean reg = new ClubRegistrationsBean();
		UserBean opt = userRepository.findByEmail("cl3vul42006@gmail.com");
		reg.setBookClub(club1);
		reg.setCheckIn(true);
		reg.setRegisteredAt(LocalDateTime.now().plusDays(-3).plusHours(-1));
		reg.setStatus(1);
		reg.setUser(opt);
		clubRegistrationsRepository.save(reg);

		log.info("已生成 1 筆讀書會");
	}

	public void createReviews(List<BooksBean> books, List<UserBean> users, int reviewsPerBook) {
		if (books.isEmpty())
			return;

		int total = 0;
		for (BooksBean book : books) {
			for (int i = 0; i < reviewsPerBook; i++) {
				ReviewBean r = new ReviewBean();
				UserBean u = users.get(random.nextInt(users.size()));
				r.setUserId(u.getUserId());
				r.setBookId(book.getBookId());
				r.setRating(random.nextInt(3) + 3); // 3-5 星
				r.setComment("這本書真的不錯！模擬評價 #" + i);
				r.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
				reviewRepository.save(r);
				total++;
			}
		}
		log.info("已生成 {} 筆評價", total);
	}
}
