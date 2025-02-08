package pl.excellentapp.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.excellentapp.brewery.order.domain.order.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BeerInventoryEvent {

    private UUID orderId;
    private Map<UUID, Integer> beers = new HashMap<>();

    public BeerInventoryEvent(Order order) {
        this.orderId = order.getId();
        order.getItems().forEach(orderItem -> beers.put(orderItem.getBeerId(), orderItem.getOrderedQuantity()));
    }
}
