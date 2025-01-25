package pl.excellentapp.brewery.order.application;

import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;

import java.util.List;
import java.util.UUID;

public interface OrderFactory {

    Order createOrder(UUID customerId, List<OrderItem> orderItems);
}
