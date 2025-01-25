package pl.excellentapp.brewery.order.application;

import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {

    List<Order> findAll();

    Optional<Order> findById(UUID id);

    Order create(UUID customerId, List<OrderItem> orderItems);

    Order update(UUID orderId, List<OrderItem> orderItems);

    void delete(UUID orderId);

    void markAsPickedUp(UUID id);

    void cancelOrder(UUID id);
}
