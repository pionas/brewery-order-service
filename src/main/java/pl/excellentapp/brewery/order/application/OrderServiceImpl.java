package pl.excellentapp.brewery.order.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.excellentapp.brewery.order.domain.exception.OrderNotFoundException;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;
import pl.excellentapp.brewery.order.domain.order.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderFactory orderFactory;
    private final OrderEventPublisher orderEventPublisher;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order create(UUID customerId, List<OrderItem> orderItems) {
        Order order = orderFactory.createOrder(customerId, orderItems);
        orderEventPublisher.publishOrderCreatedEvent(order);
        return orderRepository.save(order);
    }

    @Override
    public Order update(UUID orderId, Order order) {
        final var currentOrder = getOrderById(orderId);
        if (currentOrder.getOrderStatus() == OrderStatus.NEW) {
            currentOrder.setOrderStatus(OrderStatus.READY);
            orderEventPublisher.publishOrderCreatedEvent(currentOrder);
        }
        return orderRepository.save(currentOrder);
    }

    @Override
    public void delete(UUID orderId) {
        getOrderById(orderId);
        orderRepository.deleteById(orderId);
    }

    @Override
    public Order markAsPickedUp(UUID id) {
        Order order = getOrderById(id);
        if (order.getOrderStatus() == OrderStatus.READY) {
            order.setOrderStatus(OrderStatus.PICKED_UP);
            orderEventPublisher.publishOrderPickedUpEvent(order);
            return orderRepository.save(order);
        }
        throw new IllegalStateException("Order is not ready for pickup");
    }

    @Override
    public Order cancelOrder(UUID id) {
        Order order = getOrderById(id);
        if (order.getOrderStatus() != OrderStatus.PICKED_UP) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderEventPublisher.publishOrderCancelledEvent(order);
            return orderRepository.save(order);
        }
        throw new IllegalStateException("Delivered orders cannot be cancelled");
    }

    private Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order Not Found. UUID: " + orderId));
    }
}
