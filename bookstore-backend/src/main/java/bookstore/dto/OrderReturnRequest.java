package bookstore.dto;

import lombok.Data;

@Data
public class OrderReturnRequest {
    private Integer orderId;
    private String reason;
    private String description;
}
