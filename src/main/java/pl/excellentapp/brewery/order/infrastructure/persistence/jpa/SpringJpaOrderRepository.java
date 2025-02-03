package pl.excellentapp.brewery.order.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface SpringJpaOrderRepository extends JpaRepository<OrderEntity, UUID> {
}
