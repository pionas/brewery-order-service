package pl.excellentapp.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class OrderValidatedEvent extends Event {

    private UUID orderId;
    private BeerOrderStatus status = BeerOrderStatus.VALIDATED;

    public OrderValidatedEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
