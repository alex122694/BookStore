package bookstore.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String recipientName;
    private String recipientPhone;
    private String deliveryMethod; // 配送方式: "HOME"(宅配) 或 "STORE"(超商)
    private String address; // 地址或門市名稱
    private String paymentMethod;
    private Integer couponId;
}
