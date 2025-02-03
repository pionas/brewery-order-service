package pl.excellentapp.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent extends Event {

    private UUID orderId;
    private UUID customerId;
    private BigDecimal totalPrice;
    private BeerOrderStatus status;
}
