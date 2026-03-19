package bookstore.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import bookstore.aop.BusinessException;
import bookstore.bean.BookImageBean;
import bookstore.bean.BooksBean;
import bookstore.bean.GenreBean;
import bookstore.repository.BookRepository;
import bookstore.repository.GenreRepository;
import jakarta.transaction.Transactional;

@Service
public class bookService {

	private static final Logger log = LoggerFactory.getLogger(bookService.class);
	@Autowired
	private BookRepository bookRepo;

	@Autowired
	private GenreRepository genreRepo;

	@Value("${file.upload.dir:C:/uploads/book-images/}")
	private String uploadDir;

	// ------select all books-----------
	@Transactional
	public List<BooksBean> selectAllBooks() {
		List<BooksBean> bookList = bookRepo.findAll();

		log.info("查詢成功 - 共{}筆資料", bookList.size());
		return bookList;

	}

	// Select book by id-----------------
	@Transactional
	public BooksBean selectBookByIdS(Integer bookIdStr) {
		Optional<BooksBean> opt = bookRepo.findById(bookIdStr);

		if (opt.isPresent()) {
			log.info("查詢單筆成功 - 查詢ID:{}", bookIdStr);
			return opt.get();
		} else {
			log.warn("查詢單筆失敗 - 無該ID相關書籍資料 - 查詢ID:{}", bookIdStr);
			throw new BusinessException(404, "無該ID相關書籍資料");
		}

	}

	// select by isbn
	@Transactional
	public BooksBean selectBookByisbn(String isbnStr) {
		Optional<BooksBean> opt = bookRepo.findByIsbn(isbnStr);
		if (opt.isPresent()) {
			log.info("查詢ISBN成功 - 查詢ISBN:{}", isbnStr);
			return opt.get();
		} else {
			log.warn("查詢ISBN失敗 - 無相關ISBN書籍 - 查詢ISBN:{}", isbnStr);
			throw new BusinessException(404, "無相關ISBN書籍資料");
		}
	}

	// isbn for Ajax
	public Boolean existsByIsbn(String isbnStr) {
		if (isbnStr.trim().length() != 13) {
			throw new BusinessException(400, "ISBN必須為13位數字");
		}
		return bookRepo.findByIsbn(isbnStr).isPresent();
	}


	// select by onShelf
	@Transactional
	public Integer selectOnShelfById(Integer bookId) {
		Optional<BooksBean> opt = bookRepo.findById(bookId);
		if (opt.isPresent()) {
			Integer status = opt.get().getOnShelf();
			log.info("查詢成功 - 查詢ID:{} - 狀態:{}", bookId, status);
			return status;

		} else {
			log.warn("查詢失敗 - 無該ID相關書籍資料 - 查詢ID:{}", bookId);
			throw new BusinessException(404, "無該ID相關書籍資料");
		}
	}

	// insertbook
	@Transactional
	public BooksBean insertBook(BooksBean book, MultipartFile file) throws IOException {

		// 書名檢查
		if (book.getBookName() == null || book.getBookName().trim().isEmpty()) {
			throw new BusinessException(400, "新增失敗：書名不可為空白");
		}

		// ISBN 格式與唯一性檢查
		String isbnRegex = "^\\d{13}$";
		if (book.getIsbn() == null || !book.getIsbn().matches(isbnRegex)) {
			throw new BusinessException(400, "新增失敗：ISBN 格式錯誤，必須為 13 位數字");
		}

		Optional<BooksBean> isbnCheck = bookRepo.findByIsbn(book.getIsbn());
		if (isbnCheck.isPresent()) {
			throw new BusinessException(409, "新增失敗：已存在相同 ISBN 的書籍");
		}

		// 價格檢查
		if (book.getPrice() == null || book.getPrice().compareTo(BigDecimal.ZERO) <= 0
				|| book.getPrice().remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
			throw new BusinessException(400, "新增失敗：價格必須為正整數");
		}

		// 庫存檢查
		if (book.getStock() == null || book.getStock() < 0) {
			book.setStock(0);
		}
		
		book.setOnShelf(0);

		// --- 2. 處理 Genre (多對多關聯) ---
		if (book.getGenres() != null && !book.getGenres().isEmpty()) {
			Set<GenreBean> managedGenres = new HashSet<>();
			for (GenreBean genre : book.getGenres()) {
				Optional<GenreBean> genreOpt = genreRepo.findById(genre.getGenreId());
				if (genreOpt.isPresent()) {
					managedGenres.add(genreOpt.get());
				} else {
					throw new BusinessException(404, "新增失敗：找不到指定的類型 ID: " + genre.getGenreId());
				}
			}
			book.setGenres(managedGenres);
		}

		// --- 3. 處理圖片上傳 (OneToOne) ---
		if (file != null && !file.isEmpty()) {
			// 產生唯一檔名
			String originalFilename = file.getOriginalFilename();
			String ext = (originalFilename != null && originalFilename.contains("."))
					? originalFilename.substring(originalFilename.lastIndexOf("."))
					: ".jpg";
			String newFilename = UUID.randomUUID().toString() + ext;

			// 建立目錄與存檔
			File destFile = new File(uploadDir, newFilename);
			if (!destFile.getParentFile().exists()) {
				destFile.getParentFile().mkdirs();
			}
			file.transferTo(destFile); // 存入硬碟

			// 建立圖片物件並設定關聯
			BookImageBean imageBean = new BookImageBean();
			imageBean.setImageUrl(newFilename); 

			// 雙向關聯設定 (重要)
			imageBean.setBook(book); 
			book.setBookImageBean(imageBean); 
		}

		// --- 4. 儲存 ---
		return bookRepo.save(book);
	}

	// update Check
	private void updateBasicFields(BooksBean existing, BooksBean incoming) {

		
		// 書名
		if (incoming.getBookName() != null) {
			if (incoming.getBookName().trim().isEmpty())
				throw new BusinessException(400, "書名不可為空白");
			existing.setBookName(incoming.getBookName());
		}

		// 作者
		if (incoming.getAuthor() != null) {
			if (incoming.getAuthor().trim().isEmpty())
				throw new BusinessException(400, "作者不可為空白");
			existing.setAuthor(incoming.getAuthor());
		}

		// 出版社
		if (incoming.getPress() != null) {
			if (incoming.getPress().trim().isEmpty())
				throw new BusinessException(400, "出版社不可為空白");
			existing.setPress(incoming.getPress());
		}

		// 價格 (驗證正整數)
		if (incoming.getPrice() != null) {
			if (incoming.getPrice().compareTo(BigDecimal.ZERO) <= 0
					|| incoming.getPrice().remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
				throw new BusinessException(400, "價格必須為正整數");
			}
			existing.setPrice(incoming.getPrice());
		}

		// ISBN (格式驗證 + 唯一性檢查)
		if (incoming.getIsbn() != null) {
			if (!incoming.getIsbn().matches("^[0-9]{13}$")) {
				throw new BusinessException(400, "ISBN 格式錯誤，必須為 13 位數字");
			}
			Optional<BooksBean> isbnCheck = bookRepo.findByIsbn(incoming.getIsbn());
			if (isbnCheck.isPresent() && !isbnCheck.get().getBookId().equals(existing.getBookId())) {
				throw new BusinessException(409, "此 ISBN 已被其他書籍使用");
			}
			existing.setIsbn(incoming.getIsbn());
		}

		// 庫存
		if (incoming.getStock() != null) {
			if (incoming.getStock() < 0)
				throw new BusinessException(400, "庫存量不可小於 0");
			existing.setStock(incoming.getStock());
		}

		// 狀態
		if (incoming.getOnShelf() != null) {
			existing.setOnShelf(incoming.getOnShelf());
		}

		// 譯者與簡述
		if (incoming.getTranslator() != null)
			existing.setTranslator(incoming.getTranslator());
		if (incoming.getShortDesc() != null)
			existing.setShortDesc(incoming.getShortDesc());
	}

	private void updateBookImage(BooksBean book, MultipartFile file) throws IllegalStateException, IOException {
		String originalName = file.getOriginalFilename();
		String ext = (originalName != null && originalName.contains("."))
				? originalName.substring(originalName.lastIndexOf("."))
				: ".jpg";

		String newFileName = UUID.randomUUID().toString() + ext;

		File destFile = new File(uploadDir, newFileName);

		file.transferTo(destFile);

		BookImageBean img = book.getBookImageBean();
		if (img == null) {
			img = new BookImageBean();
			img.setBook(book);
			book.setBookImageBean(img);
		}
		img.setImageUrl(newFileName);
	}

	// UpDate Book-----------------
	@Transactional
	public BooksBean updateBook(Integer bookId, BooksBean book, MultipartFile file)
			throws IllegalStateException, IOException {

		Optional<BooksBean> opt = bookRepo.findById(bookId);
		if (opt.isEmpty()) {
			throw new BusinessException(404, "修改失敗：找不到 ID 為 " + bookId + " 的書籍");
		}

		BooksBean existingBook = opt.get();

		if (book.getOnShelf() == 2) {
			throw new BusinessException(400, "該書籍處於封存狀態，不可修改");
		}

		updateBasicFields(existingBook, book);

		if (book.getGenres() != null) {
			Set<GenreBean> newManagedGenres = new HashSet<GenreBean>();
			for (GenreBean genre : book.getGenres()) {
				Optional<GenreBean> genreOpt = genreRepo.findById(genre.getGenreId());
				if (genreOpt.isPresent()) {
					newManagedGenres.add(genreOpt.get());
				} else {
					throw new BusinessException(404, "修改失敗:找不到類型ID" + genre.getGenreId());
				}
			}
			existingBook.setGenres(newManagedGenres);
		}
		if (file != null && !file.isEmpty()) {
			updateBookImage(existingBook, file);
		}
		return bookRepo.save(existingBook);

	}

	// delete book-----------------
	@Transactional
	public void deleteBookById(Integer bookIdStr) {
		Optional<BooksBean> opt = bookRepo.findById(bookIdStr);
		if (opt.isEmpty()) {
			log.warn("刪除失敗 - 無該ID相關書籍資料 - 書籍ID:{}", bookIdStr);
			throw new BusinessException(400, "刪除失敗 - 無該ID相關書籍資料");
		}
		try {
			bookRepo.deleteById(bookIdStr);
		} catch (Exception e) {
			log.error("刪除失敗 - 伺服器異常，請聯繫開發人員");
			throw new BusinessException(500, "刪除失敗 - 伺服器異常，請聯繫開發人員");
		}

	}

	// update onShelf
	@Transactional
	public BooksBean updateOnShelfStatus(Integer bookId, Integer newStatus) {
		Optional<BooksBean> opt = bookRepo.findById(bookId);
		if (!opt.isPresent()) {
			log.warn("查詢失敗 - 無ID:{} 相關書籍資料", bookId);
			throw new BusinessException(404, "查詢失敗 - 無該ID相關書籍資料");
		}
		BooksBean book = opt.get();
		if (book.getOnShelf() == 2) {
			log.warn("修改失敗 - 已封存書籍不可上架");
			throw new BusinessException(404, "修改失敗 - 已封存的書籍不可上架");
		}
		book.setOnShelf(newStatus);
		return book;
	}

	// archive book
	@Transactional
	public BooksBean archiveBook(Integer bookId) {
		Optional<BooksBean> opt = bookRepo.findById(bookId);
		if (!opt.isPresent()) {
			log.warn("查詢失敗 - 查無ID:{} 相關書籍資料", bookId);
			throw new BusinessException(404, "查無此ID相關書籍資料");
		} else {
			BooksBean book = opt.get();
			book.setOnShelf(2);
			bookRepo.save(book);
			log.info("書籍 ID:{}已成功封存", bookId);
			return book;
		}
	}

	// unarchive book
	@Transactional
	public BooksBean unarchiveBook(Integer bookId) {
		Optional<BooksBean> opt = bookRepo.findById(bookId);
		if (!opt.isPresent()) {
			log.warn("查詢失敗 -無:ID{}相關書籍資料", bookId);
			throw new BusinessException(404, "查無此ID相關書籍資料");
		} else {
			BooksBean book = opt.get();
			book.setOnShelf(0);
			bookRepo.save(book);
			log.info("書籍ID:{}已成功解封", bookId);
			return book;
		}
	}

	// 封存檢查(訂單/評價)
	@Transactional
	public boolean isBookAvailable(Integer bookId) {
		Optional<BooksBean> opt = bookRepo.findById(bookId);
		if (!opt.isPresent()) {
			log.warn("查詢失敗 -無:ID{}相關書籍資料", bookId);
			throw new BusinessException(404, "無該ID相關書籍");
		}
		Integer onShelf = opt.get().getOnShelf();
		if (onShelf == null || onShelf != 1) {
			log.info("書籍ID:{} 狀態:{} 處於封存狀態，禁止新增關聯資料", bookId, onShelf);
			return false;
		}
		return true;
	}

	// 修改庫存 - 供Log調用
	@Transactional
	public BooksBean updateStock(Integer bookId, Integer stock) {
		Optional<BooksBean> opt = bookRepo.findById(bookId);
		if (opt.isPresent()) {
			BooksBean book = opt.get();
			book.setStock(stock);
			return bookRepo.save(book);
		} else {
			log.warn("修改失敗 - 無該ID相關書籍資料");
			throw new BusinessException(404, "修改失敗 - 無該ID相關書籍資料");
		}
	}

	@Transactional
	public List<GenreBean> getAllGenres() {
		return genreRepo.findAll();
	}

}
