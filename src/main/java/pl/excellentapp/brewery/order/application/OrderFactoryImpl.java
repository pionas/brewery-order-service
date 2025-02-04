package pl.excellentapp.brewery.order.application;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventory;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventoryService;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.utils.DateTimeProvider;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
@Service
@ExtensionMethod(CollectionUtils.class)
class OrderFactoryImpl implements OrderFactory {

    private final BeerInventoryService beerInventoryService;
    private final DateTimeProvider dateTimeProvider;

    @Override
    public Order createOrder(@NonNull UUID customerId, @NonNull List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be empty");
        }
        Order order = new Order();
        order.setVersion(1L);
        order.setId(null);
        order.setCustomerId(customerId);
        order.setOrderDateTime(dateTimeProvider.now());
        for (OrderItem item : orderItems) {
            BeerInventory beerInventory = beerInventoryService.getInventory(item.getBeerId());
            if (beerInventory.getOnHand() < item.getOrderedQuantity()) {
                log.info("Not enough inventory for beer: {}. Order need {} but is {}", beerInventory.getName(), item.getOrderedQuantity(), beerInventory.getOnHand());
            }
            item.updatePrice(beerInventory.getPrice());
            item.reserve(beerInventory.getOnHand());
        }
        order.setItems(orderItems);
        order.calculateTotalPrice();
        order.initStatus();
        return order;
    }
}
