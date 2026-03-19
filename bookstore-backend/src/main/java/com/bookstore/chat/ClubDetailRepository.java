package com.bookstore.chat;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookstore.chat.ClubDetail;
import java.util.List;


public interface ClubDetailRepository extends JpaRepository<ClubDetail	, Integer> {

	Optional<ClubDetail>  findByMainClub_ClubId(Integer clubId);
}
