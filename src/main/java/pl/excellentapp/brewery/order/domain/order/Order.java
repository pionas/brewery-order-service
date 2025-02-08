package pl.excellentapp.brewery.order.domain.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    private UUID id;
    private UUID customerId;
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private OffsetDateTime orderDateTime;
    private BeerOrderStatus orderStatus;

    private Long version;
    private OffsetDateTime lastModifiedDate;

    public void initStatus() {
        boolean allItemsReserved = items.stream()
                .allMatch(OrderItem::isFullyReserved);

        if (allItemsReserved) {
            orderStatus = BeerOrderStatus.ALLOCATED;
        }
    }

    public boolean isReady() {
        return BeerOrderStatus.ALLOCATED == orderStatus;
    }

    public void calculateTotalPrice() {
        this.totalPrice = items.stream()
                .map(OrderItem::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void reserve(UUID beerId, Integer stock) {
        items.stream()
                .filter(orderItem -> orderItem.containsId(beerId))
                .findAny()
                .ifPresent(orderItem -> orderItem.reserve(stock));
        initStatus();
        calculateTotalPrice();
    }
}
