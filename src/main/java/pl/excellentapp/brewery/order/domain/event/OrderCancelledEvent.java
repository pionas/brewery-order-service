package pl.excellentapp.brewery.order.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.excellentapp.brewery.order.domain.order.OrderStatus;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent extends Event {

    private UUID orderId;
    private OrderStatus status = OrderStatus.CANCELLED;

    public OrderCancelledEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
