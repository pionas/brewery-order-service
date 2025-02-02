package pl.excellentapp.brewery.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderAllocatedEvent extends Event {

    private UUID orderId;
    private BeerOrderStatus status = BeerOrderStatus.ALLOCATED;

    public OrderAllocatedEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
