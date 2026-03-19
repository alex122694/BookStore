package bookstore.dao.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;

import bookstore.bean.ReviewBean;
import bookstore.dao.ReviewDAO;
import bookstore.util.HibernateUtil;

public class ReviewsDAOImpl implements ReviewDAO {

    // =============================
    // 查詢所有評價
    // =============================
    @Override
    public List<ReviewBean> selectAllReviews() {

        List<ReviewBean> reviews = null;

        try {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();

            session.beginTransaction();

            String hql = "SELECT r FROM ReviewBean r JOIN FETCH r.user JOIN FETCH r.book";
            reviews = session.createQuery(hql, ReviewBean.class).getResultList();

            session.getTransaction().commit();

            System.out.println("Hibernate selectAllReviews size = " + reviews.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return reviews;
    }

    // =============================
    // 依 reviewId 查詢單筆
    // =============================
    @SuppressWarnings("removal")
	@Override
    public ReviewBean selectReviewById(Integer reviewId) {

        ReviewBean review = null;

        try {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();

            session.beginTransaction();
            
            String hql = "SELECT r FROM ReviewBean r " +
                    "JOIN FETCH r.user " +
                    "JOIN FETCH r.book " +
                    "WHERE r.reviewId = :id";
            review = session.createQuery(hql, ReviewBean.class)
                    .setParameter("id", reviewId)
                    .uniqueResult();

            session.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return review;
    }

    // =============================
    // 新增評價
    // =============================
    @Override
    public int insertReview(ReviewBean review) {

        int result = 0;

        try {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();

            session.beginTransaction();

            // 若 createdAt 沒值，Java 端補
            if (review.getCreatedAt() == null) {
                review.setCreatedAt(LocalDateTime.now());
            }

            session.persist(review);

            session.getTransaction().commit();

            result = 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // =============================
    // 更新評價
    // =============================
    @Override
    public int updateReview(ReviewBean review) {

        int result = 0;

        try {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();

            session.beginTransaction();

            session.merge(review);

            session.getTransaction().commit();

            result = 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // =============================
    // 刪除評價
    // =============================
    @SuppressWarnings("removal")
	@Override
    public int deleteReview(Integer reviewId) {

        int result = 0;

        try {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();

            session.beginTransaction();

            ReviewBean review = session.get(ReviewBean.class, reviewId);
            if (review != null) {
                session.remove(review);
                result = 1;
            }

            session.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
