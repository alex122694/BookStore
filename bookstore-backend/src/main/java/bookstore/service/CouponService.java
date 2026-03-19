package bookstore.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bookstore.aop.BusinessException;
import bookstore.bean.CouponBean;
import bookstore.bean.UserCouponBean;
import bookstore.repository.CouponRepository;
import bookstore.repository.UserCouponRepository;
import bookstore.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserRepository userRepository;

    // 後台管理員建立優惠券種類
    public CouponBean createCoupon(CouponBean coupon) {
        if (couponRepository.existsByCouponCode(coupon.getCouponCode())) {
            throw new BusinessException(400, "優惠券代碼已存在: " + coupon.getCouponCode());
        }
        coupon.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        coupon.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        if (coupon.getIsAvailable() == null) {
            coupon.setIsAvailable(1);
        }
        return couponRepository.save(coupon);
    }

    // 後台管理員更新優惠券
    public CouponBean updateCoupon(CouponBean coupon) {
        CouponBean existing = couponRepository.findById(coupon.getCouponId())
                .orElseThrow(() -> new BusinessException(404, "找不到優惠券"));

        // Update fields
        existing.setCouponName(coupon.getCouponName());
        // Code is usually unique/immutable, but if allowed to change, check uniqueness
        // existing.setCouponCode(coupon.getCouponCode());
        existing.setDiscountAmount(coupon.getDiscountAmount());
        existing.setMinSpend(coupon.getMinSpend());
        existing.setIsAvailable(coupon.getIsAvailable());

        existing.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return couponRepository.save(existing);
    }

    // 檢查使用者是否領取過此優惠券
    public boolean hasClaimed(Integer userId, Integer couponId) {
        return userCouponRepository.findByUserIdAndCouponId(userId, couponId).isPresent();
    }

    // 使用者領取優惠券
    public UserCouponBean claimCoupon(Integer userId, String code) {
        // 1. 驗證使用者
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "找不到使用者"));

        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(400, "優惠券代碼不能為空");
        }

        String validCode = code.trim();

        // 2. 依據優惠碼尋找優惠券
        CouponBean couponDef = couponRepository.findByCouponCode(validCode)
                .orElseThrow(() -> new BusinessException(404, "無效的優惠券代碼"));

        // Check if available
        if (couponDef.getIsAvailable() != 1) {
            throw new BusinessException(400, "此優惠券目前無法領取");
        }

        // 3. 檢查使用者是否領取過此優惠券
        if (hasClaimed(userId, couponDef.getCouponId())) {
            throw new BusinessException(400, "您已經領取過此優惠券");
        }

        // 4. 建立使用者優惠券
        UserCouponBean userCoupon = new UserCouponBean();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponDef.getCouponId()); // Link via ID or Object
        userCoupon.setStatus(0); // Unused
        userCoupon.setReceivedAt(new Timestamp(System.currentTimeMillis()));

        return userCouponRepository.save(userCoupon);
    }

    // 取得使用者所擁有的優惠券
    public List<UserCouponBean> getCouponsByUserId(Integer userId) {
        return userCouponRepository.findByUserId(userId);
    }

    // 取得使用者所擁有的未使用優惠券
    public List<UserCouponBean> getUnusedCouponsByUserId(Integer userId) {
        return userCouponRepository.findUnusedByUserId(userId);
    }

    // 取得所有優惠券種類
    public List<CouponBean> getAllCoupons() {
        return couponRepository.findAll();
    }

    // 使用優惠券
    public void useCoupon(Integer userCouponId) {
        UserCouponBean userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new BusinessException(404, "找不到優惠券記錄"));

        if (userCoupon.getStatus() == 1) {
            throw new BusinessException(400, "優惠券已使用");
        }

        userCoupon.setStatus(1);
        userCoupon.setUsedAt(new Timestamp(System.currentTimeMillis()));
        userCouponRepository.save(userCoupon);
    }

    // 後台管理員取得所有使用者所擁有的優惠券
    public List<UserCouponBean> getAllUserCoupons() {
        return userCouponRepository.findAll();
    }
}
