package pl.excellentapp.brewery.order.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.excellentapp.brewery.order.domain.order.OrderStatus;

import java.util.UUID;

@Getter

@NoArgsConstructor
@AllArgsConstructor
public class OrderPickedUpEvent extends Event {

    private UUID orderId;
    private OrderStatus status = OrderStatus.PICKED_UP;

    public OrderPickedUpEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
