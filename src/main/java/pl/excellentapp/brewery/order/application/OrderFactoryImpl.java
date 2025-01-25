package pl.excellentapp.brewery.order.application;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventory;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventoryService;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.domain.order.OrderStatus;
import pl.excellentapp.brewery.order.utils.DateTimeProvider;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
@Service
class OrderFactoryImpl implements OrderFactory {

    private final BeerInventoryService beerInventoryService;
    private final DateTimeProvider dateTimeProvider;

    @Override
    public Order createOrder(UUID customerId, List<OrderItem> orderItems) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOrderDateTime(dateTimeProvider.now());
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            BeerInventory beerInventory = beerInventoryService.getInventory(item.getBeerId());
            if (beerInventory.getOnHand() < item.getOrderedQuantity()) {
                log.info("Not enough inventory for beer: {}. Order need {} but is {}", beerInventory.getName(), item.getOrderedQuantity(), beerInventory.getOnHand());
            }
            item.reserved(beerInventory.getOnHand(), beerInventory.getPrice());
            totalPrice = totalPrice.add(item.getTotalPrice());
        }
        order.setTotalPrice(totalPrice);
        order.setItems(orderItems);
        boolean allItemsReserved = order.getItems().stream()
                .allMatch(OrderItem::isFullyReserved);

        if (allItemsReserved) {
            order.setOrderStatus(OrderStatus.READY);
        } else {
            order.setOrderStatus(OrderStatus.NEW);
        }
        return order;
    }
}
