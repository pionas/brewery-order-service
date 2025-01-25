package pl.excellentapp.brewery.order.application;

import lombok.RequiredArgsConstructor;
import pl.excellentapp.brewery.order.domain.event.OrderCancelledEvent;
import pl.excellentapp.brewery.order.domain.event.OrderCreatedEvent;
import pl.excellentapp.brewery.order.domain.event.OrderEventChannel;
import pl.excellentapp.brewery.order.domain.event.OrderPickedUpEvent;
import pl.excellentapp.brewery.order.domain.event.OrderReadyEvent;
import pl.excellentapp.brewery.order.domain.order.Order;

@RequiredArgsConstructor
public class OrderEventPublisher {

    private final OrderEventChannel eventChannel;

    public void publishOrderCreatedEvent(Order order) {
        eventChannel.publish(OrderCreatedEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .totalPrice(order.getTotalPrice())
                .status(order.getOrderStatus())
                .build()
        );
    }

    public void publishOrderPickedUpEvent(Order order) {
        eventChannel.publish(
                new OrderPickedUpEvent(order.getId())
        );
    }

    public void publishOrderCancelledEvent(Order order) {
        eventChannel.publish(
                new OrderCancelledEvent(order.getId())
        );
    }

    public void publishOrderReadyEvent(Order order) {
        eventChannel.publish(
                new OrderReadyEvent(order.getId())
        );
    }
}
