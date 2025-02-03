package pl.excellentapp.brewery.order.infrastructure.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.domain.order.OrderPage;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderItemRequest;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderPagedList;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderRequest;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderResponse;

import java.util.List;

@Mapper
public interface OrderRestMapper {

    Order map(OrderRequest orderRequest);

    OrderResponse map(Order order);

    List<OrderResponse> mapOrders(List<Order> all);

    @Mapping(target = "orderedQuantity", source = "quantity")
    @Mapping(target = "reservedQuantity", ignore = true)
    @Mapping(target = "price", ignore = true)
    OrderItem map(OrderItemRequest orderItemRequest);

    List<OrderItem> mapToOrderItemList(List<OrderItemRequest> items);

    @Mapping(target = "content", source = "orders")
    @Mapping(target = "number", source = "pageNumber")
    @Mapping(target = "size", source = "pageSize")
    @Mapping(target = "totalElements", source = "total")
    OrderPagedList map(OrderPage orderPage);
}
