package pl.excellentapp.brewery.order.infrastructure.rest.api.mapper;

import org.mapstruct.Mapper;
import pl.excellentapp.brewery.order.domain.Order;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderRequest;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderResponse;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrdersResponse;

import java.util.List;

@Mapper
public interface OrderRestMapper {

    Order map(OrderRequest orderRequest);

    OrderResponse map(Order order);

    List<OrderResponse> mapOrders(List<Order> all);

    default OrdersResponse map(List<Order> all) {
        return OrdersResponse.builder()
                .orders(mapOrders(all))
                .build();
    }
}
