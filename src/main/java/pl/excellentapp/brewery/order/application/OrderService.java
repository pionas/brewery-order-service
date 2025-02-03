package pl.excellentapp.brewery.order.application;

import lombok.NonNull;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderPage;

import java.util.Optional;
import java.util.UUID;

public interface OrderService {

    OrderPage list(@NonNull Integer pageNumber, @NonNull Integer pageSize);

    Optional<Order> findById(UUID id);

    void delete(UUID orderId);

    void markAsPickedUp(UUID id);

    void cancelOrder(UUID id);
}
