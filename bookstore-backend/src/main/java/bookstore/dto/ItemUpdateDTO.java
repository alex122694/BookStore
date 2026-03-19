package bookstore.dto;

import lombok.Data;

@Data
public class ItemUpdateDTO {
    private Integer orderItemId;
    private Integer quantity;
}
