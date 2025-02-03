package pl.excellentapp.brewery.order.application;

import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;

import java.util.List;

public interface OrderUpdated {

    Order update(Order order, List<OrderItem> orderItems);

}
