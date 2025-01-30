package pl.excellentapp.brewery.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.excellentapp.brewery.order.domain.order.OrderStatus;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderReadyEvent extends Event {

    private UUID orderId;
    private OrderStatus status = OrderStatus.READY;

    public OrderReadyEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
