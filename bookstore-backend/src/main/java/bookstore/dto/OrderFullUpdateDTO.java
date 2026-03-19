package bookstore.dto;

import java.util.List;
import lombok.Data;

@Data
public class OrderFullUpdateDTO {
    private Integer orderId;
    private String recipientAt;
    private String phone;
    private String address;
    private String paymentMethod;
    private String deliveryMethod;
    private String paymentStatus;
    private String orderStatus;
    private List<ItemUpdateDTO> items;
}
