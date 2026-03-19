package bookstore.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import bookstore.aop.BusinessException;
import bookstore.bean.BooksBean;
import bookstore.bean.GenreBean;
import bookstore.service.bookService;

@RestController
@RequestMapping("/api/books")
public class BookController {

	@Autowired
	private bookService bookService;

	@GetMapping("/booksIndex")
	public String bookIndex(Model model) {
		return "books/booksIndex";
	}

	@GetMapping("/getAllBooks")
	public ResponseEntity<List<BooksBean>> getAllBooks(Model model) {
		List<BooksBean> bookList = bookService.selectAllBooks();
		return ResponseEntity.ok(bookList);
	}

	// @GetMapping("/getBooksPaged")
	// public ResponseEntity<Page<BooksBean>> getBooksPaged(
	// @RequestParam(defaultValue = "0") int page,
	// @RequestParam(defaultValue = "10") int size,
	// @RequestParam(defaultValue = "bookId") String sortBy,
	// @RequestParam(defaultValue = "false") boolean sortDesc) {
	// Page<BooksBean> bookPage = bookService.selectBooksPaged(page, size, sortBy,
	// sortDesc);
	// return ResponseEntity.ok(bookPage);
	// }

	@GetMapping("/getBook/{bookId}")
	public ResponseEntity<BooksBean> selectBookById(@PathVariable("bookId") Integer bookId) {
		if (bookId == null) {
			throw new BusinessException(400, "格式錯誤:ID不可為空白");
		}
		BooksBean book = bookService.selectBookByIdS(bookId);
		if (book != null) {
			return ResponseEntity.ok(book);
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/genres")
	public ResponseEntity<List<GenreBean>> InsertPage(Model model) {
		List<GenreBean> genreList = bookService.getAllGenres();
		return ResponseEntity.ok(genreList);
	}

	@PostMapping("/insert")
	public ResponseEntity<BooksBean> insertBook(@RequestPart("book") BooksBean book,
			@RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
		BooksBean newBook = bookService.insertBook(book, file);
		return ResponseEntity.ok(newBook);
	}

	@PutMapping("/update/{bookId}")
	public ResponseEntity<BooksBean> updateBook(@PathVariable Integer bookId, @RequestPart("book") BooksBean book,
			@RequestPart(value = "file", required = false) MultipartFile file)
			throws IllegalStateException, IOException {
		BooksBean updateBook = bookService.updateBook(bookId, book, file);
		return ResponseEntity.ok(updateBook);
	}

	@PutMapping("/updateStatus/{bookId}/{newStatus}")
	public ResponseEntity<BooksBean> updateOnShelf(@PathVariable Integer bookId, @PathVariable Integer newStatus) {
		BooksBean book = bookService.updateOnShelfStatus(bookId, newStatus);
		return ResponseEntity.ok(book);
	}

	@DeleteMapping("/delete/{bookId}")
	public ResponseEntity<String> deleteBook(@PathVariable Integer bookId) {
		bookService.deleteBookById(bookId);
		return ResponseEntity.noContent().build();
	}

	@ResponseBody
	@GetMapping("/isbnCheck")
	public ResponseEntity<Boolean> checkIsbn(@RequestParam("isbn") String isbn) {
		Boolean exists = bookService.existsByIsbn(isbn);
		return ResponseEntity.ok(exists);
	}

	@PutMapping("/updateStock/{bookId}/{newStock}")
	public ResponseEntity<BooksBean> updateStock(@PathVariable Integer bookId, @PathVariable Integer newStock) {
		BooksBean book = bookService.updateStock(bookId, newStock);
		return ResponseEntity.ok(book);
	}

	@PutMapping("/archiveBook/{bookId}")
	public ResponseEntity<BooksBean> archiveBook(@PathVariable("bookId") Integer bookId) {
		BooksBean book = bookService.archiveBook(bookId);
		return ResponseEntity.ok(book);
	}

	@PutMapping("/unarchiveBook/{bookId}")
	public ResponseEntity<BooksBean> unarchiveBook(@PathVariable("bookId") Integer bookId) {
		BooksBean book = bookService.unarchiveBook(bookId);
		return ResponseEntity.ok(book);
	}
}
