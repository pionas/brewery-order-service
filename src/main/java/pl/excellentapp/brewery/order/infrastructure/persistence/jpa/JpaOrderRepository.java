package pl.excellentapp.brewery.order.infrastructure.persistence.jpa;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
    public Pair<List<Order>, Integer> list(Integer pageNumber, Integer pageSize) {
        final var pageRequest = PageRequest.of(pageNumber, pageSize);
        final var orderPage = springJpaOrderRepository.findAll(pageRequest);
        List<Order> beers = orderPage.getContent().stream().map(orderMapper::map).toList();
        return Pair.of(beers, orderPage.getTotalPages());
    }

    @Override
    public Order save(Order order) {
        return orderMapper.map(springJpaOrderRepository.saveAndFlush(orderMapper.map(order)));
    }

    @Override
    @Transactional
    public synchronized Optional<Order> findById(UUID id) {
        return springJpaOrderRepository.findById(id)
                .map(orderMapper::map);
    }

    @Override
    public void deleteById(UUID id) {
        springJpaOrderRepository.deleteById(id);
    }
}
