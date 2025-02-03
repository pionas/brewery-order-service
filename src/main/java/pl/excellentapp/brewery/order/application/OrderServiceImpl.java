package pl.excellentapp.brewery.order.application;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import pl.excellentapp.brewery.order.domain.exception.OrderNotFoundException;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.domain.order.OrderPage;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderUpdated orderUpdated;
    private final OrderEventPublisher orderEventPublisher;

    @Override
    public OrderPage list(@NonNull Integer pageNumber, @NonNull Integer pageSize) {
        return OrderPage.of(orderRepository.list(pageNumber, pageSize), pageNumber, pageSize);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order update(UUID orderId, List<OrderItem> orderItems) {
        final var order = orderUpdated.update(getOrderById(orderId), orderItems);
        if (order.isReady()) {
            orderEventPublisher.publishOrderReadyEvent(order);
        }
        return orderRepository.save(order);
    }

    @Override
    public void delete(UUID orderId) {
        getOrderById(orderId);
        orderRepository.deleteById(orderId);
    }

    @Override
    public void markAsPickedUp(UUID id) {
        Order order = getOrderById(id);
        if (order.getOrderStatus() == BeerOrderStatus.ALLOCATED) {
            order.setOrderStatus(BeerOrderStatus.PICKED_UP);
            orderEventPublisher.publishOrderPickedUpEvent(order);
            orderRepository.save(order);
            return;
        }
        throw new IllegalStateException("Order is not ready for pickup");
    }

    @Override
    public void cancelOrder(UUID id) {
        Order order = getOrderById(id);
        if (order.getOrderStatus() != BeerOrderStatus.PICKED_UP) {
            order.setOrderStatus(BeerOrderStatus.CANCELLED_BY_USER);
            orderEventPublisher.publishOrderCancelledEvent(order);
            orderRepository.save(order);
            return;
        }
        throw new IllegalStateException("Delivered orders cannot be cancelled");
    }

    private Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order Not Found. UUID: " + orderId));
    }
}
