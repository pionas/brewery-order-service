package pl.excellentapp.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.excellentapp.brewery.order.domain.order.Order;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderValidatedEvent extends Event {

    private UUID orderId;
    private Set<UUID> beers = new HashSet<>();

    public OrderValidatedEvent(Order order) {
        this.orderId = order.getId();
        order.getItems().forEach(orderItem -> beers.add(orderItem.getBeerId()));
    }
}
