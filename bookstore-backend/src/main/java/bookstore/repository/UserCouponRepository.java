package bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bookstore.bean.UserCouponBean;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCouponBean, Integer> {

    // Find all coupons for a user
    List<UserCouponBean> findByUserId(Integer userId);

    // Find specific coupon type for a user (to check if already claimed)
    @Query("SELECT uc FROM UserCouponBean uc WHERE uc.userId = :userId AND uc.couponId = :couponId")
    Optional<UserCouponBean> findByUserIdAndCouponId(@Param("userId") Integer userId,
            @Param("couponId") Integer couponId);

    // Find unused coupons for a user
    @Query("SELECT uc FROM UserCouponBean uc WHERE uc.userId = :userId AND uc.status = 0")
    List<UserCouponBean> findUnusedByUserId(@Param("userId") Integer userId);

    // Count usage of a specific coupon definition
    long countByCouponIdAndStatus(Integer couponId, Integer status);

    // Count claims of a specific coupon definition
    long countByCouponId(Integer couponId);
}
