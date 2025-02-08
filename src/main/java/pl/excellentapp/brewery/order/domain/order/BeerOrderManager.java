package pl.excellentapp.brewery.order.domain.order;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BeerOrderManager {

    String BEER_ORDER_ID_HEADER = "orderId";

    Order newOrder(UUID customerId, List<OrderItem> orderItems);

    void processValidationResult(UUID beerOrderId, Boolean isValid);

    void orderAllocationPassed(UUID orderId, Map<UUID, Integer> beers);

    void orderAllocationFailed(UUID orderId, Map<UUID, Integer> beers);

    void orderPickedUp(UUID id);

    void cancel(UUID id);

}
