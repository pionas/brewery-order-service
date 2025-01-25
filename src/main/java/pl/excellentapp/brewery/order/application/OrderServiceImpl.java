package pl.excellentapp.brewery.order.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.excellentapp.brewery.order.domain.Order;
import pl.excellentapp.brewery.order.domain.OrderRepository;
import pl.excellentapp.brewery.order.domain.exception.OrderNotFoundException;
import pl.excellentapp.brewery.order.utils.DateTimeProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DateTimeProvider dateTimeProvider;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order create(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order update(UUID orderId, Order order) {
        final var currentOrder = getOrderById(orderId);
        // TODO
        return orderRepository.save(currentOrder);
    }

    @Override
    public void delete(UUID orderId) {
        getOrderById(orderId);
        orderRepository.deleteById(orderId);
    }

    private Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order Not Found. UUID: " + orderId));
    }
}
