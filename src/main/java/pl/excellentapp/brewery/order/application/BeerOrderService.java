package pl.excellentapp.brewery.order.application;

import pl.excellentapp.brewery.order.domain.order.Order;

import java.util.UUID;

public interface BeerOrderService {

    Order placeOrder(UUID customerId, Order order);

    Order getOrderById(UUID customerId, UUID orderId);

    void pickupOrder(UUID customerId, UUID orderId);
}