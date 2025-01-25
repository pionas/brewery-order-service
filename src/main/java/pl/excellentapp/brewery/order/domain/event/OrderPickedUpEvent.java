package pl.excellentapp.brewery.order.domain.event;

import lombok.Getter;
import pl.excellentapp.brewery.order.domain.order.OrderStatus;

import java.util.UUID;

@Getter
public class OrderPickedUpEvent {

    private final UUID orderId;
    private final OrderStatus status = OrderStatus.PICKED_UP;

    public OrderPickedUpEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
