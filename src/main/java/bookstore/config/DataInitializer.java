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

	// 訂單初始資料
	@Transactional
	public void createOrders(List<UserBean> users, List<BooksBean> books) {
		// 取得書籍與會員 (確保索引在安全範圍內)
		UserBean admin = users.get(0);
		UserBean user1 = users.get(1);
		UserBean user4 = users.get(4);
		UserBean user5 = users.get(5);
		UserBean user9 = users.get(9);

		BooksBean b1 = books.get(0);
		BooksBean b2 = books.get(1);
		BooksBean b4 = books.get(3);

		// --- Order 1 ---
		Orders o1 = new Orders();
		o1.setUserBean(admin);
		o1.setTotalAmount(new BigDecimal("1300.00"));
		o1.setPaymentMethod("貨到付款");
		o1.setPaymentStatus("已付款");
		o1.setOrderStatus("已送達");
		o1.setRecipientAt("陳總管理");
		o1.setAddress("台醫店 (006598) - 台北市中正區中山南路７號１樓");
		o1.setPhone("0910111222");
		o1.setDeliveryMethod("超商取貨");
		o1.setShippingFee(BigDecimal.ZERO);
		o1.setFinalAmount(new BigDecimal("1300.00"));
		o1.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-02 13:28:00"));
		ordersRepository.save(o1);

		OrderItem i1_1 = new OrderItem();
		i1_1.setOrders(o1);
		i1_1.setBooksBean(b1);
		i1_1.setQuantity(2);
		i1_1.setPrice(new BigDecimal("500.00"));
		i1_1.setSubtotal(new BigDecimal("1000.00"));
		orderItemRepository.save(i1_1);

		OrderItem i1_2 = new OrderItem();
		i1_2.setOrders(o1);
		i1_2.setBooksBean(b2);
		i1_2.setQuantity(1);
		i1_2.setPrice(new BigDecimal("300.00"));
		i1_2.setSubtotal(new BigDecimal("300.00"));
		orderItemRepository.save(i1_2);

		// 針對 Order 1 的退貨 (ID 1104)
		OrderReturnBean ret1 = new OrderReturnBean();
		ret1.setOrders(o1);
		ret1.setReason("商品有瑕疵");
		ret1.setDescription("");
		orderReturnRepository.save(ret1);

		// --- Order 2 ---
		Orders o2 = new Orders();
		o2.setUserBean(user4);
		o2.setTotalAmount(new BigDecimal("1900.00"));
		o2.setPaymentMethod("貨到付款");
		o2.setPaymentStatus("已付款");
		o2.setOrderStatus("已完成");
		o2.setRecipientAt("張雅雯");
		o2.setAddress("北市杭州店 (2001) - 台北市大安區杭州南路二段５號");
		o2.setPhone("0940777888");
		o2.setDeliveryMethod("超商取貨");
		o2.setShippingFee(BigDecimal.ZERO);
		o2.setFinalAmount(new BigDecimal("1900.00"));
		o2.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-02 13:28:00"));
		ordersRepository.save(o2);

		OrderItem i2_1 = new OrderItem();
		i2_1.setOrders(o2);
		i2_1.setBooksBean(b2);
		i2_1.setQuantity(3);
		i2_1.setPrice(new BigDecimal("500.00"));
		i2_1.setSubtotal(new BigDecimal("1500.00"));
		orderItemRepository.save(i2_1);

		// 針對 Order 2 的退貨 (ID 1105)
		OrderReturnBean ret2 = new OrderReturnBean();
		ret2.setOrders(o2);
		ret2.setReason("收到錯誤商品");
		ret2.setDescription("我將退貨");
		orderReturnRepository.save(ret2);

		// --- Order 4 ---
		Orders o4 = new Orders();
		o4.setUserBean(user5);
		o4.setTotalAmount(new BigDecimal("500.00"));
		o4.setPaymentMethod("貨到付款");
		o4.setPaymentStatus("已付款");
		o4.setOrderStatus("已完成");
		o4.setRecipientAt("李國豪");
		o4.setAddress("建盛門市 (131386) - 新竹市東區建中一路52號1樓");
		o4.setPhone("0950999000");
		o4.setDeliveryMethod("超商取貨");
		o4.setShippingFee(BigDecimal.ZERO);
		o4.setFinalAmount(new BigDecimal("500.00"));
		o4.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-02 13:28:00"));
		ordersRepository.save(o4);

		OrderItem i4_1 = new OrderItem();
		i4_1.setOrders(o4);
		i4_1.setBooksBean(b1);
		i4_1.setQuantity(1);
		i4_1.setPrice(new BigDecimal("500.00"));
		i4_1.setSubtotal(new BigDecimal("500.00"));
		orderItemRepository.save(i4_1);

		// --- Order 5 ---
		Orders o5 = new Orders();
		o5.setUserBean(user9);
		o5.setTotalAmount(new BigDecimal("1300.00"));
		o5.setPaymentMethod("信用卡");
		o5.setPaymentStatus("已付款");
		o5.setOrderStatus("已完成");
		o5.setRecipientAt("林光磊");
		o5.setAddress("台北市信義區管理中心");
		o5.setPhone("0971554238");
		o5.setDeliveryMethod("宅配到府");
		o5.setShippingFee(BigDecimal.ZERO);
		o5.setFinalAmount(new BigDecimal("1300.00"));
		o5.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-05 14:51:00"));
		ordersRepository.save(o5);

		OrderItem i5_1 = new OrderItem();
		i5_1.setOrders(o5);
		i5_1.setBooksBean(b1);
		i5_1.setQuantity(2);
		i5_1.setPrice(new BigDecimal("500.00"));
		i5_1.setSubtotal(new BigDecimal("1000.00"));
		orderItemRepository.save(i5_1);

		OrderItem i5_2 = new OrderItem();
		i5_2.setOrders(o5);
		i5_2.setBooksBean(b2);
		i5_2.setQuantity(1);
		i5_2.setPrice(new BigDecimal("300.00"));
		i5_2.setSubtotal(new BigDecimal("300.00"));
		orderItemRepository.save(i5_2);

		// --- Order 6 ---
		Orders o6 = new Orders();
		o6.setUserBean(admin);
		o6.setTotalAmount(new BigDecimal("500.00"));
		o6.setPaymentMethod("貨到付款");
		o6.setPaymentStatus("未付款");
		o6.setOrderStatus("已取消");
		o6.setRecipientAt("陳總管理");
		o6.setAddress("建盛門市 (131386) - 新竹市東區建中一路52號1樓");
		o6.setPhone("0990707808");
		o6.setDeliveryMethod("超商取貨");
		o6.setShippingFee(BigDecimal.ZERO);
		o6.setFinalAmount(new BigDecimal("500.00"));
		o6.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-05 14:51:00"));
		ordersRepository.save(o6);

		OrderItem i6_1 = new OrderItem();
		i6_1.setOrders(o6);
		i6_1.setBooksBean(b2);
		i6_1.setQuantity(1);
		i6_1.setPrice(new BigDecimal("500.00"));
		i6_1.setSubtotal(new BigDecimal("500.00"));
		orderItemRepository.save(i6_1);

		// 針對 Order 6 的退貨 (ID 1067)
		OrderReturnBean ret3 = new OrderReturnBean();
		ret3.setOrders(o6);
		ret3.setReason("改變心意");
		ret3.setDescription("");
		orderReturnRepository.save(ret3);

		// --- Order 8 ---
		Orders o8 = new Orders();
		o8.setUserBean(user5);
		o8.setTotalAmount(new BigDecimal("500.00"));
		o8.setPaymentMethod("貨到付款");
		o8.setPaymentStatus("已付款");
		o8.setOrderStatus("已完成");
		o8.setRecipientAt("李國豪");
		o8.setAddress("建盛門市 (131386) - 新竹市東區建中一路52號1樓");
		o8.setPhone("0947158247");
		o8.setDeliveryMethod("超商取貨");
		o8.setShippingFee(BigDecimal.ZERO);
		o8.setFinalAmount(new BigDecimal("500.00"));
		o8.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-07 14:51:00"));
		ordersRepository.save(o8);

		OrderItem i8_1 = new OrderItem();
		i8_1.setOrders(o8);
		i8_1.setBooksBean(b1);
		i8_1.setQuantity(1);
		i8_1.setPrice(new BigDecimal("500.00"));
		i8_1.setSubtotal(new BigDecimal("500.00"));
		orderItemRepository.save(i8_1);

		// --- Order 13 ---
		Orders o13 = new Orders();
		o13.setUserBean(user4);
		o13.setTotalAmount(new BigDecimal("400.00"));
		o13.setPaymentMethod("貨到付款");
		o13.setPaymentStatus("已付款");
		o13.setOrderStatus("聯絡中");
		o13.setRecipientAt("張雅雯");
		o13.setAddress("台醫店 (006598) - 台北市中正區中山南路７號１樓");
		o13.setPhone("0912345678");
		o13.setDeliveryMethod("超商取貨");
		o13.setShippingFee(BigDecimal.ZERO);
		o13.setFinalAmount(new BigDecimal("400.00"));
		o13.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-10 10:56:00"));
		ordersRepository.save(o13);

		OrderItem i13_1 = new OrderItem();
		i13_1.setOrders(o13);
		i13_1.setBooksBean(b1);
		i13_1.setQuantity(1);
		i13_1.setPrice(new BigDecimal("400.00"));
		i13_1.setSubtotal(new BigDecimal("400.00"));
		orderItemRepository.save(i13_1);

		// --- Order 14 ---
		Orders o14 = new Orders();
		o14.setUserBean(user1);
		o14.setTotalAmount(new BigDecimal("400.00"));
		o14.setPaymentMethod("貨到付款");
		o14.setPaymentStatus("已付款");
		o14.setOrderStatus("已完成");
		o14.setRecipientAt("趙後勤");
		o14.setAddress("北市杭州店 (2001) - 台北市大安區杭州南路二段５號");
		o14.setPhone("0912345678");
		o14.setDeliveryMethod("超商取貨");
		o14.setShippingFee(BigDecimal.ZERO);
		o14.setFinalAmount(new BigDecimal("400.00"));
		o14.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-12 11:04:00"));
		ordersRepository.save(o14);

		OrderItem i14_1 = new OrderItem();
		i14_1.setOrders(o14);
		i14_1.setBooksBean(b1);
		i14_1.setQuantity(10);
		i14_1.setPrice(new BigDecimal("400.00"));
		i14_1.setSubtotal(new BigDecimal("4000.00"));
		orderItemRepository.save(i14_1);

		// --- Order 15 ---
		Orders o15 = new Orders();
		o15.setUserBean(admin);
		o15.setTotalAmount(new BigDecimal("700.00"));
		o15.setPaymentMethod("貨到付款");
		o15.setPaymentStatus("已付款");
		o15.setOrderStatus("已完成");
		o15.setRecipientAt("陳總管理");
		o15.setAddress("台醫店 (006598) - 台北市中正區中山南路７號１樓");
		o15.setPhone("0990707808");
		o15.setDeliveryMethod("超商取貨");
		o15.setShippingFee(BigDecimal.ZERO);
		o15.setFinalAmount(new BigDecimal("700.00"));
		o15.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-15 11:06:00"));
		ordersRepository.save(o15);

		OrderItem i15_1 = new OrderItem();
		i15_1.setOrders(o15);
		i15_1.setBooksBean(b2);
		i15_1.setQuantity(2);
		i15_1.setPrice(new BigDecimal("350.00"));
		i15_1.setSubtotal(new BigDecimal("700.00"));
		orderItemRepository.save(i15_1);

		// --- Order 17 ---
		Orders o17 = new Orders();
		o17.setUserBean(user5);
		o17.setTotalAmount(new BigDecimal("1120.00"));
		o17.setPaymentMethod("貨到付款");
		o17.setPaymentStatus("已付款");
		o17.setOrderStatus("已完成");
		o17.setRecipientAt("李國豪");
		o17.setAddress("建盛門市 (131386) - 新竹市東區建中一路52號1樓");
		o17.setPhone("0912345678");
		o17.setDeliveryMethod("超商取貨");
		o17.setShippingFee(BigDecimal.ZERO);
		o17.setFinalAmount(new BigDecimal("1120.00"));
		o17.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-18 11:09:00"));
		ordersRepository.save(o17);

		OrderItem i17_1 = new OrderItem();
		i17_1.setOrders(o17);
		i17_1.setBooksBean(b2);
		i17_1.setQuantity(2);
		i17_1.setPrice(new BigDecimal("400.00"));
		i17_1.setSubtotal(new BigDecimal("800.00"));
		orderItemRepository.save(i17_1);

		OrderItem i17_2 = new OrderItem();
		i17_2.setOrders(o17);
		i17_2.setBooksBean(b4);
		i17_2.setQuantity(1);
		i17_2.setPrice(new BigDecimal("320.00"));
		i17_2.setSubtotal(new BigDecimal("320.00"));
		orderItemRepository.save(i17_2);

		log.info("已匯入完整的 Orders, OrderItem 及 OrderReturnBean");
	}

	@Transactional
	public List<CouponBean> createCoupons() {
		List<CouponBean> list = new ArrayList<>();

		// --- Coupon 7: read10 ---
		CouponBean c7 = new CouponBean();
		c7.setCouponCode("read10");
		c7.setCreatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2025-10-15T15:03:48")));
		c7.setDiscountAmount(new BigDecimal("10.00"));
		c7.setMinSpend(new BigDecimal("299.00"));
		c7.setCouponName("森呼吸．入林禮");
		c7.setIsAvailable(0);
		c7.setUpdatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-29T13:52:07")));
		list.add(couponRepository.save(c7)); // 儲存並加入 List

		// --- Coupon 8: read20 ---
		CouponBean c8 = new CouponBean();
		c8.setCouponCode("read20");
		c8.setCreatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-11-28T15:28:31")));
		c8.setDiscountAmount(new BigDecimal("20.00"));
		c8.setMinSpend(new BigDecimal("399.00"));
		c8.setCouponName("森呼吸．林間漫步");
		c8.setIsAvailable(1);
		c8.setUpdatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-29T13:52:14")));
		list.add(couponRepository.save(c8));

		// --- Coupon 9: read100 ---
		CouponBean c9 = new CouponBean();
		c9.setCouponCode("read100");
		c9.setCreatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-12-12T15:29:02")));
		c9.setDiscountAmount(new BigDecimal("100.00"));
		c9.setMinSpend(new BigDecimal("899.00"));
		c9.setCouponName("森呼吸．讀享券");
		c9.setIsAvailable(1);
		c9.setUpdatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-28T15:59:55")));
		list.add(couponRepository.save(c9));

		// --- Coupon 10: read30 ---
		CouponBean c10 = new CouponBean();
		c10.setCouponCode("read30");
		c10.setCreatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-12-21T15:35:51")));
		c10.setDiscountAmount(new BigDecimal("30.00"));
		c10.setMinSpend(new BigDecimal("599.00"));
		c10.setCouponName("森呼吸．慢讀時光");
		c10.setIsAvailable(1);
		c10.setUpdatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-28T16:25:15")));
		list.add(couponRepository.save(c10));

		// --- Coupon 11: read150 ---
		CouponBean c11 = new CouponBean();
		c11.setCouponCode("read150");
		c11.setCreatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-01T13:37:42")));
		c11.setDiscountAmount(new BigDecimal("150.00"));
		c11.setMinSpend(new BigDecimal("1299.00"));
		c11.setCouponName("森呼吸．一起讀");
		c11.setIsAvailable(1);
		c11.setUpdatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-29T13:37:42")));
		list.add(couponRepository.save(c11));

		// --- Coupon 12: read99 ---
		CouponBean c12 = new CouponBean();
		c12.setCouponCode("read99");
		c12.setCreatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-22T11:32:53")));
		c12.setDiscountAmount(new BigDecimal("99.00"));
		c12.setMinSpend(new BigDecimal("499.00"));
		c12.setCouponName("森呼吸．植行力");
		c12.setIsAvailable(0);
		c12.setUpdatedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-31T11:33:40")));
		list.add(couponRepository.save(c12));

		log.info("已新增 6 筆優惠券資料並回傳清單");
		return list;
	}

	@Transactional
	public void createUserCoupons(List<UserBean> users, List<CouponBean> coupons) {
		// --- UserCoupon 1: Coupon 7 (read10), User 4 (張雅雯) ---
		UserCouponBean uc1 = new UserCouponBean();
		uc1.setCouponBean(coupons.get(0)); // Coupon ID 7 假設在 list index 0
		uc1.setUserBean(users.get(4));
		uc1.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2025-12-10T15:29:34")));
		uc1.setStatus(1);
		uc1.setUsedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-03T15:56:41")));
		userCouponRepository.save(uc1);

		// --- UserCoupon 2: Coupon 10 (read30), User 2 (王曉明) ---
		UserCouponBean uc2 = new UserCouponBean();
		uc2.setCouponBean(coupons.get(3)); // Coupon ID 10
		uc2.setUserBean(users.get(2));
		uc2.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-28T15:36:03")));
		uc2.setStatus(0);
		uc2.setUsedAt(null);
		userCouponRepository.save(uc2);

		// --- UserCoupon 3: Coupon 11 (read150), User 5 (李國豪) ---
		UserCouponBean uc3 = new UserCouponBean();
		uc3.setCouponBean(coupons.get(4)); // Coupon ID 11
		uc3.setUserBean(users.get(5));
		uc3.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-29T13:37:51")));
		uc3.setStatus(0);
		uc3.setUsedAt(null);
		userCouponRepository.save(uc3);

		// --- UserCoupon 4: Coupon 8 (read20), User 4 (張雅雯) ---
		UserCouponBean uc4 = new UserCouponBean();
		uc4.setCouponBean(coupons.get(1)); // Coupon ID 8
		uc4.setUserBean(users.get(4));
		uc4.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-29T13:45:09")));
		uc4.setStatus(0);
		uc4.setUsedAt(null);
		userCouponRepository.save(uc4);

		// --- UserCoupon 5: Coupon 9 (read100), User 8 (假設對應林先生) ---
		UserCouponBean uc5 = new UserCouponBean();
		uc5.setCouponBean(coupons.get(2)); // Coupon ID 9
		uc5.setUserBean(users.get(8));
		uc5.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-29T13:48:50")));
		uc5.setStatus(1);
		uc5.setUsedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-15T09:29:00")));
		userCouponRepository.save(uc5);

		// --- UserCoupon 6: Coupon 10 (read30), User 1 (宋泓孝/Admin) ---
		UserCouponBean uc6 = new UserCouponBean();
		uc6.setCouponBean(coupons.get(3));
		uc6.setUserBean(users.get(1));
		uc6.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-31T11:45:37")));
		uc6.setStatus(0);
		uc6.setUsedAt(null);
		userCouponRepository.save(uc6);

		// --- UserCoupon 7: Coupon 11 (read150), User 1 ---
		UserCouponBean uc7 = new UserCouponBean();
		uc7.setCouponBean(coupons.get(4));
		uc7.setUserBean(users.get(1));
		uc7.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-31T11:45:44")));
		uc7.setStatus(0);
		uc7.setUsedAt(null);
		userCouponRepository.save(uc7);

		// --- UserCoupon 8: Coupon 7 (read10), User 1 ---
		UserCouponBean uc8 = new UserCouponBean();
		uc8.setCouponBean(coupons.get(0));
		uc8.setUserBean(users.get(1));
		uc8.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-31T11:45:49")));
		uc8.setStatus(1);
		uc8.setUsedAt(null);
		userCouponRepository.save(uc8);

		// --- UserCoupon 9: Coupon 8 (read20), User 3 (林小姐) ---
		UserCouponBean uc9 = new UserCouponBean();
		uc9.setCouponBean(coupons.get(1));
		uc9.setUserBean(users.get(3));
		uc9.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-31T11:46:13")));
		uc9.setStatus(0);
		uc9.setUsedAt(null);
		userCouponRepository.save(uc9);

		// --- UserCoupon 10: Coupon 10 (read30), User 3 ---
		UserCouponBean uc10 = new UserCouponBean();
		uc10.setCouponBean(coupons.get(3));
		uc10.setUserBean(users.get(3));
		uc10.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-31T11:46:33")));
		uc10.setStatus(0);
		uc10.setUsedAt(null);
		userCouponRepository.save(uc10);

		// --- UserCoupon 11: Coupon 11 (read150), User 3 ---
		UserCouponBean uc11 = new UserCouponBean();
		uc11.setCouponBean(coupons.get(4));
		uc11.setUserBean(users.get(3));
		uc11.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-31T11:46:38")));
		uc11.setStatus(0);
		uc11.setUsedAt(null);
		userCouponRepository.save(uc11);

		// --- UserCoupon 12: Coupon 8 (read20), User 8 ---
		UserCouponBean uc12 = new UserCouponBean();
		uc12.setCouponBean(coupons.get(1));
		uc12.setUserBean(users.get(8));
		uc12.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-31T11:47:01")));
		uc12.setStatus(1);
		uc12.setUsedAt(null);
		userCouponRepository.save(uc12);

		// --- UserCoupon 13: Coupon 10 (read30), User 5 (李國豪) ---
		UserCouponBean uc13 = new UserCouponBean();
		uc13.setCouponBean(coupons.get(3));
		uc13.setUserBean(users.get(5));
		uc13.setReceivedAt(java.sql.Timestamp.valueOf(LocalDateTime.parse("2026-01-31T11:47:37")));
		uc13.setStatus(0);
		uc13.setUsedAt(null);
		userCouponRepository.save(uc13);

		log.info("已手動匯入 13 筆會員領取優惠券關聯資料");
	}

	// 評價初始資料
	@Transactional
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
}
