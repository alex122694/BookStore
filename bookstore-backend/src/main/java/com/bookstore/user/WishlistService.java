package com.bookstore.user;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookstore.book.BooksBean;
import com.bookstore.user.UserBean;
import com.bookstore.user.WishlistBean;
import com.bookstore.user.WishlistRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class WishlistService {
	
	private final WishlistRepository wishlistRepo;
	
	public boolean toggleWishBooks(Integer userId, Integer bookId) {
		Optional<WishlistBean> existingWishBooks = wishlistRepo.findByUserUserIdAndBookBookId(userId, bookId);
		
		if (existingWishBooks.isPresent()) {
			wishlistRepo.deleteByUserUserIdAndBookBookId(userId, bookId);
			return false;
		} else {
			WishlistBean wish = WishlistBean.builder()
					.user(UserBean.builder().userId(userId).build())
					.book(new BooksBean())
					.createdAt(new Date())
					.build();
			
			wish.getBook().setBookId(bookId);
			
			wishlistRepo.save(wish);
			return true;
		}
	}
	
	public List<WishlistBean> getWishlist(Integer userId) {
		return wishlistRepo.findByUserUserIdOrderByCreatedAtDesc(userId);
	}
	
	public void removeFromWishList(Integer wishlistId) {
		wishlistRepo.deleteById(wishlistId);
	}
	
	public boolean isFavorited(Integer userId, Integer bookId) {
		return wishlistRepo.findByUserUserIdAndBookBookId(userId, bookId).isPresent();
	}
	
}
