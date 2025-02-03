package pl.excellentapp.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderAllocationFailureEvent extends Event {

    private UUID orderId;
    private BeerOrderStatus status = BeerOrderStatus.ALLOCATION_EXCEPTION;

    public OrderAllocationFailureEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
