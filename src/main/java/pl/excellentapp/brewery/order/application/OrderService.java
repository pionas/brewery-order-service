package pl.excellentapp.brewery.order.application;

import pl.excellentapp.brewery.order.domain.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {

    List<Order> findAll();

    Optional<Order> findById(UUID id);

    Order create(Order order);

    Order update(UUID orderId, Order order);

    void delete(UUID orderId);
}
