package pl.excellentapp.brewery.order.domain.order;

import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Data
public class OrderItem {

    private final UUID beerId;
    private final Integer orderedQuantity;
    private BigDecimal price;
    private Integer reservedQuantity;

    public OrderItem(@NonNull UUID beerId, @NonNull Integer orderedQuantity) {
        if (orderedQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number");
        }
        this.beerId = beerId;
        this.orderedQuantity = orderedQuantity;
        this.price = BigDecimal.ZERO;
        this.reservedQuantity = 0;
    }

    public void reserve(Integer availableQuantity) {
        if (availableQuantity == null || availableQuantity < 0) {
            throw new IllegalArgumentException("Available quantity must be non-negative");
        }
        this.reservedQuantity += Math.min(this.orderedQuantity - this.reservedQuantity, availableQuantity);
    }

    public void updatePrice(@NonNull BigDecimal price) {
        if (BigDecimal.ZERO.compareTo(price) >= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        this.price = price;
    }

    public BigDecimal calculateTotalPrice() {
        return price.multiply(new BigDecimal(orderedQuantity));
    }

    public boolean isFullyReserved() {
        return Objects.equals(orderedQuantity, reservedQuantity);
    }

    public boolean isNotFullyReserved() {
        return !Objects.equals(orderedQuantity, reservedQuantity);
    }

    public boolean containsId(UUID beerId) {
        return Objects.equals(this.beerId, beerId);
    }
}
