package pl.excellentapp.brewery.order.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    List<Order> findAll();

    Order save(Order order);

    Optional<Order> findById(UUID id);

    void deleteById(UUID id);
}
