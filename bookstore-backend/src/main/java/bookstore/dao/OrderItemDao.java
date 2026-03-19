package bookstore.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import bookstore.bean.OrderItem;

public class OrderItemDao {

	private Session session;

	public OrderItemDao(Session session) {
		this.session = session;
	}

	// 新增訂單明細
	public void insertOrderItem(OrderItem item) {
		session.persist(item);
	}

	// 依訂單編號更新訂單明細
	public void updateOrderItem(OrderItem item) {
		session.merge(item);
	}

	// 依訂單明細ID刪除訂單明細
	public void deleteOrderItemById(Integer orderItemId) {
		OrderItem item = session.get(OrderItem.class, orderItemId);
		if (item != null) {
			session.remove(item);
		}
	}

	// 依訂單ID刪除所有明細
	public void deleteOrderItemsByOrderId(Integer orderId) {
		Query<?> query = session.createQuery("delete from OrderItem where orderId = :orderId");
		query.setParameter("orderId", orderId);
		query.executeUpdate();
	}

	// 依訂單明細ID查詢訂單明細
	public OrderItem findOrderItemById(Integer orderItemId) {
		return session.get(OrderItem.class, orderItemId);
	}

	// 依訂單ID查詢所有明細
	public List<OrderItem> findOrderItemsByOrderId(Integer orderId) {
		Query<OrderItem> query = session.createQuery("from OrderItem i where i.orders.orderId = :orderId", OrderItem.class);		query.setParameter("orderId", orderId);
		return query.list();
	}

	// 查詢所有訂單明細
	public List<OrderItem> findAllOrderItems() {
		Query<OrderItem> query = session.createQuery("from OrderItem", OrderItem.class);
		return query.list();
	}
}
