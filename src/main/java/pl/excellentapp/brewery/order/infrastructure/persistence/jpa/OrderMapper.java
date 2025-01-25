package pl.excellentapp.brewery.order.infrastructure.persistence.jpa;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper
interface OrderMapper {

    List<Order> map(Iterable<OrderEntity> orderEntities);

    @Mapping(target = "orderDateTime", source = "createdDate")
    Order map(OrderEntity orderEntity);

    @Mapping(target = "beerId", source = "id.beerId")
    OrderItem map(OrderItemEntity orderItemEntity);

    @Mapping(target = "createdDate", source = "orderDateTime")
    OrderEntity map(Order order);

    @Mapping(target = "id.beerId", source = "beerId")
    OrderItemEntity map(OrderItem orderItem);

    default Timestamp map(OffsetDateTime value) {
        return value == null ? null : Timestamp.from(value.toInstant());
    }

    default OffsetDateTime map(Timestamp value) {
        return value == null ? null : value.toInstant()
                .atOffset(OffsetDateTime.now().getOffset());
    }
}
