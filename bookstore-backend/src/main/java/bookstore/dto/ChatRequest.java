package bookstore.dto;

import java.util.List;

public class ChatRequest {
    private String message;
    private List<BookDtoSimple> books;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<BookDtoSimple> getBooks() {
        return books;
    }

    public void setBooks(List<BookDtoSimple> books) {
        this.books = books;
    }

    public static class BookDtoSimple {
        private String bookName;
        private String author;
        private Integer price;
        private String shortDesc;

        public String getBookName() {
            return bookName;
        }

        public void setBookName(String bookName) {
            this.bookName = bookName;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
        }

        public String getShortDesc() {
            return shortDesc;
        }

        public void setShortDesc(String shortDesc) {
            this.shortDesc = shortDesc;
        }
    }
}
