package pl.excellentapp.brewery.order.infrastructure.persistence.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface SpringJpaOrderRepository extends CrudRepository<OrderEntity, UUID> {
}
