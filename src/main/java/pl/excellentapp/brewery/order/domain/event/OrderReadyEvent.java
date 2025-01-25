package pl.excellentapp.brewery.order.domain.event;

import pl.excellentapp.brewery.order.domain.order.OrderStatus;

import java.util.UUID;

public class OrderReadyEvent {

    private final UUID orderId;
    private final OrderStatus status = OrderStatus.READY;

    public OrderReadyEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
