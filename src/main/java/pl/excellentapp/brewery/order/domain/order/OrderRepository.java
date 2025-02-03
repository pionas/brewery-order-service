package pl.excellentapp.brewery.order.domain.order;

import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Pair<List<Order>, Integer> list(Integer pageNumber, Integer pageSize);

    Order save(Order order);

    Optional<Order> findById(UUID id);

    void deleteById(UUID id);
}
