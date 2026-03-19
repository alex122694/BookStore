package bookstore.dao;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import bookstore.bean.Orders;

public class OrdersDao {

	private Session session;

	public OrdersDao(Session session) {
		this.session = session;
	}

	// 新增訂單
	public void insertOrder(Orders order) {

		Timestamp now = new Timestamp(System.currentTimeMillis());
	    
	    if (order.getCreatedAt() == null) {
	        order.setCreatedAt(now);
	    }
	    if (order.getUpdatedAt() == null) {
	        order.setUpdatedAt(now);
	    }
	    
	    session.persist(order);
	}

	// 更新訂單
	public void updateOrder(Orders order) {
		order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		session.merge(order);
	}

	// 刪除訂單
	public void deleteOrderById(Integer orderId) {
		Orders order = session.get(Orders.class, orderId);
		if (order != null) {
			session.remove(order);
		}
	}

	// 查詢所有訂單
	public List<Orders> findAllOrders() {
		Query<Orders> query = session.createQuery("from Orders", Orders.class);
		return query.list();
	}
	
	// 查詢未被取消的訂單(未包含已取消、退款)
	public List<Orders> findActiveOrders() {
	    // 使用 NOT IN 過濾掉不需要的狀態，並包含 NULL 的防禦判斷
	    String hql = "from Orders o where o.orderStatus not in (:s1, :s2) or o.orderStatus is null";
	    return session.createQuery(hql, Orders.class)
	                  .setParameter("s1", "已取消")
	                  .setParameter("s2", "已退款")
	                  .list();
	}

	// 僅查詢已取消、已退款的訂單(軟刪除)
	public List<Orders> findCancelAndRefundedOrders() {
	    // 使用 IN 撈取特定狀態
	    String hql = "from Orders o where o.orderStatus in (:s1, :s2)";
	    return session.createQuery(hql, Orders.class)
	                  .setParameter("s1", "已取消")
	                  .setParameter("s2", "已退款")
	                  .list();
	}

	// 依訂單ID查詢訂單
	public Orders findOrderById(Integer orderId) {
		return session.get(Orders.class, orderId);
	}

	// 依使用者ID查詢訂單
	public List<Orders> findOrdersByUserId(Integer userId) {
		Query<Orders> query = session.createQuery("from Orders o where o.user.userId = :userId", Orders.class);
		query.setParameter("userId", userId);
		return query.list();
	}
}
