package pl.excellentapp.brewery.order.infrastructure.persistence.jpa;

import org.mapstruct.Mapper;
import pl.excellentapp.brewery.order.domain.order.Order;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper
interface OrderMapper {

    List<Order> map(Iterable<OrderEntity> orderEntities);

    Order map(OrderEntity orderEntity);

    OrderEntity map(Order order);

    default Timestamp map(OffsetDateTime value) {
        return value == null ? null : Timestamp.from(value.toInstant());
    }

    default OffsetDateTime map(Timestamp value) {
        return value == null ? null : value.toInstant()
                .atOffset(OffsetDateTime.now().getOffset());
    }
}
