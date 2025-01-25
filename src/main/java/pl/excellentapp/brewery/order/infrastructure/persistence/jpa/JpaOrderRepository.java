package pl.excellentapp.brewery.order.infrastructure.persistence.jpa;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
class JpaOrderRepository implements OrderRepository {

    private final SpringJpaOrderRepository springJpaOrderRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<Order> findAll() {
        return orderMapper.map(springJpaOrderRepository.findAll());
    }

    @Override
    public Order save(Order order) {
        return orderMapper.map(springJpaOrderRepository.save(orderMapper.map(order)));
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return springJpaOrderRepository.findById(id)
                .map(orderMapper::map);
    }

    @Override
    public void deleteById(UUID id) {
        springJpaOrderRepository.deleteById(id);
    }
}
