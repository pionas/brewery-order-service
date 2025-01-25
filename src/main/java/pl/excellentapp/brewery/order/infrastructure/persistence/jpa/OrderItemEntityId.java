package pl.excellentapp.brewery.order.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class OrderItemEntityId implements Serializable {

    @Column(nullable = false)
    private UUID beerId;
    @Column(nullable = false)
    private UUID orderId;

}
