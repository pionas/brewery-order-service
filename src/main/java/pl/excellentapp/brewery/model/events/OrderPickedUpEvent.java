package pl.excellentapp.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;

import java.util.UUID;

@Getter

@NoArgsConstructor
@AllArgsConstructor
public class OrderPickedUpEvent extends Event {

    private UUID orderId;
    private BeerOrderStatus status = BeerOrderStatus.PICKED_UP;

    public OrderPickedUpEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
