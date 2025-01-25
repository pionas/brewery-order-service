package pl.excellentapp.brewery.order.domain.order;

import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Data
public class OrderItem {

    private UUID beerId;
    private Integer orderedQuantity;
    private Integer reservedQuantity = 0;
    private BigDecimal price = BigDecimal.ZERO;

    public OrderItem(@NonNull UUID beerId, @NonNull Integer orderedQuantity) {
        if (orderedQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number");
        }
        this.beerId = beerId;
        this.orderedQuantity = orderedQuantity;
    }

    public void reserved(Integer onHand, BigDecimal price) {
        this.reservedQuantity += onHand;
        this.price = price;
    }

    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(orderedQuantity));
    }

    public boolean isFullyReserved() {
        return orderedQuantity != null && Objects.equals(orderedQuantity, reservedQuantity);
    }
}
