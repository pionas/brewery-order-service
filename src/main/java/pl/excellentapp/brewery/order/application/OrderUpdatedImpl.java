package pl.excellentapp.brewery.order.application;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventory;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventoryService;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
@Service
@ExtensionMethod(CollectionUtils.class)
class OrderUpdatedImpl implements OrderUpdated {

    private final BeerInventoryService beerInventoryService;

    @Override
    public Order update(Order order, List<OrderItem> newOrderItems) {
        final var map = newOrderItems.stream()
                .collect(Collectors.toMap(OrderItem::getBeerId, item -> item));

        final var orderItems = order.getItems().stream()
                .filter(OrderItem::isNotFullyReserved)
                .filter(orderItem -> map.containsKey(orderItem.getBeerId()))
                .toList();

        for (OrderItem orderItem : orderItems) {
            BeerInventory beerInventory = beerInventoryService.getInventory(orderItem.getBeerId());
            orderItem.reserve(beerInventory.getOnHand());
        }
        order.initStatus();
        order.calculateTotalPrice();

        return order;
    }
}
