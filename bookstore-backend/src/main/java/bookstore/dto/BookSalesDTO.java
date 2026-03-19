package bookstore.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookSalesDTO {

    private String bookName;
    private Long totalQuantity;
    private Integer bookId;
    private BigDecimal price;
    private String author;
    private String coverImage;
    private String shortDesc;
    private Object genres;

    // 後台分析使用
    public BookSalesDTO(String bookName, Long totalQuantity) {
        this.bookName = bookName;
        this.totalQuantity = totalQuantity;
    }

    // 前台熱銷排名使用
    public BookSalesDTO(Integer bookId, String bookName, String author, BigDecimal price, 
            String coverImage, String shortDesc, Long totalQuantity) {
		this.bookId = bookId;
		this.bookName = bookName;
		this.author = author;
		this.price = price;
		this.coverImage = coverImage;
		this.shortDesc = shortDesc;
		this.totalQuantity = totalQuantity;
    }	

}
