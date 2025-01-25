package pl.excellentapp.brewery.order.infrastructure.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderItemRequest;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderRequest;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderResponse;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrdersResponse;

import java.util.List;

@Mapper
public interface OrderRestMapper {

    Order map(OrderRequest orderRequest);

    OrderResponse map(Order order);

    List<OrderResponse> mapOrders(List<Order> all);

    default OrdersResponse map(List<Order> orders) {
        return OrdersResponse.builder()
                .orders(mapOrders(orders))
                .build();
    }

    @Mapping(target = "orderedQuantity", source = "quantity")
    OrderItem map(OrderItemRequest orderItemRequest);

    List<OrderItem> mapToOrderItemList(List<OrderItemRequest> items);
}
