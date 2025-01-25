package pl.excellentapp.brewery.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    private UUID id;
    private UUID customerId;
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal totalPrice;
    private LocalDateTime orderDateTime;
    private OrderStatus orderStatus = OrderStatus.NEW;

}
