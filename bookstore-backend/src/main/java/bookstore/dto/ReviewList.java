package bookstore.dto;

import lombok.Data;

@Data
public class ReviewList{
	
	// 1. 書籍的基本資料
    private Integer bookId;
    private String bookName;
    private String author;
    private String press; // 出版社
    
    // 2. 評價數據
    private Integer reviewCount;
    private Double avgRating;
}
