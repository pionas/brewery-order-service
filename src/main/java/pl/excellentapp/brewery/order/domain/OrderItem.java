package pl.excellentapp.brewery.order.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderItem {

    private UUID beerId;
    private Integer quantity;
    private BigDecimal price;

    public OrderItem(@NonNull UUID beerId, @NonNull Integer quantity, @NonNull BigDecimal price) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be a positive value");
        }
        this.beerId = beerId;
        this.quantity = quantity;
        this.price = price;
    }
}
