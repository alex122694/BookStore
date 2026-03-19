package com.bookstore.coupon;

import java.util.List;
import java.util.Optional; // Added import for Optional

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookstore.coupon.CouponBean;

@Repository
public interface CouponRepository extends JpaRepository<CouponBean, Integer> {

    // Find coupon definition by code
    Optional<CouponBean> findByCouponCode(String couponCode);

    // Check if code exists
    boolean existsByCouponCode(String couponCode);
}
