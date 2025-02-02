package pl.excellentapp.brewery.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent extends Event {

    private UUID orderId;
    private BeerOrderStatus status;

    public OrderCancelledEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
