package pl.excellentapp.brewery.order.domain.event;

import lombok.Builder;
import lombok.Getter;
import pl.excellentapp.brewery.order.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
public class OrderCreatedEvent {

    private final UUID orderId;
    private final UUID customerId;
    private final BigDecimal totalPrice;
    private final OrderStatus status;
}
