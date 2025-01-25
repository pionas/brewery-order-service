package pl.excellentapp.brewery.order.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "orders_items")
public class OrderItemEntity {

    @EmbeddedId
    private OrderItemEntityId id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @MapsId("orderId")
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

}
