package bookstore.bean;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_item")
@Component
public class OrderItem implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ORDER_ITEM_ID")
	private Integer orderItemId;

	// 建立Order物件，可取代orderId欄位
	// @Column(name = "ORDER_ID")
	// private Integer orderId;

	// 整合後，可把Bookid註解掉
	// @Column(name = "BOOK_ID")
	// private Integer bookId;

	@Column(name = "QUANTITY")
	private Integer quantity;

	@Column(name = "PRICE")
	private BigDecimal price;

	@Column(name = "SUBTOTAL")
	private BigDecimal subtotal;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORDER_ID")
	@com.fasterxml.jackson.annotation.JsonIgnore
	private Orders orders;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BOOK_ID")
	private BooksBean booksBean;

	public OrderItem() {
	}

	public OrderItem(Integer orderItemId, Integer quantity, BigDecimal price,
			BigDecimal subtotal) {
		super();
		this.orderItemId = orderItemId;
		// this.orderId = orderId;
		// this.bookId = bookId;
		this.quantity = quantity;
		this.price = price;
		this.subtotal = subtotal;
	}

	public Integer getOrderItemId() {
		return orderItemId;
	}

	public void setOrderItemId(Integer orderItemId) {
		this.orderItemId = orderItemId;
	}

	// public Integer getOrderId() {
	// return orderId;
	// }
	//
	// public void setOrderId(Integer orderId) {
	// this.orderId = orderId;
	// }

	// public Integer getBookId() {
	// return bookId;
	// }
	//
	// public void setBookId(Integer bookId) {
	// this.bookId = bookId;
	// }

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
		calculateSubtotal();
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
		calculateSubtotal();
	}

	// Java無法直接將int轉為BigDecimal，必須使用new BigDecimal()
	public void setPrice(int price) {
		this.price = new BigDecimal(price);
		calculateSubtotal();
	}

	// 如果價格為小數，無法使用上面方法，所以使用String
	public void setPrice(String price) {
		this.price = new BigDecimal(price);
		calculateSubtotal();
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public Orders getOrders() {
		return orders;
	}

	public void setOrders(Orders orders) {
		this.orders = orders;
	}

	public BooksBean getBooksBean() {
		return booksBean;
	}

	public void setBooksBean(BooksBean booksBean) {
		this.booksBean = booksBean;
	}

	private void calculateSubtotal() {
		if (this.price != null && this.quantity != null) {
			// BigDecimal是物件，不支援運算子重載(不能直接用*)，multiply():乘法，Integer與BigDecimal無繼承關係，不能強制轉型，只能用new
			// BigDecimal()
			// 錯誤訊息:The operator * is undefined for the argument type(s) BigDecimal,Integer
			this.subtotal = this.price.multiply(new BigDecimal(this.quantity));
		}
	}

	@Override
	public String toString() {
		return "OrderItem [orderItemId=" + orderItemId + ", quantity="
				+ quantity + ", price=" + price + ", subtotal=" + subtotal + "]";
	}
}
