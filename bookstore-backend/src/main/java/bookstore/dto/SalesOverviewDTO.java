package bookstore.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesOverviewDTO {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalBooksSold;
}
