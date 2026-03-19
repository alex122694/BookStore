package bookstore.bean;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "book_images")
public class BookImageBean {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "image_id")
	private Integer imageId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	@JsonBackReference
	private BooksBean book;

	@Column(name = "image_url", length = 265)
	private String imageUrl;

	public BookImageBean() {
		super();
	}

	public BookImageBean(BooksBean book, String imageUrl) {
		super();
		this.book = book;
		this.imageUrl = imageUrl;
	}

	public BookImageBean(Integer imageId, BooksBean book, String imageUrl) {
		super();
		this.imageId = imageId;
		this.book = book;
		this.imageUrl = imageUrl;
	}

	public Integer getImageId() {
		return imageId;
	}

	public void setImageId(Integer imageId) {
		this.imageId = imageId;
	}

	public BooksBean getBook() {
		return book;
	}

	public void setBook(BooksBean book) {
		this.book = book;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}


}
