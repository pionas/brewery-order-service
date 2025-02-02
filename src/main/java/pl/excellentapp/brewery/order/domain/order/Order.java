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
    private BeerOrderStatus beerOrderStatus;

    private Long version;
    private OffsetDateTime lastModifiedDate;

    public void initStatus() {
        boolean allItemsReserved = items.stream()
                .allMatch(OrderItem::isFullyReserved);

        if (allItemsReserved) {
            beerOrderStatus = BeerOrderStatus.READY;
        } else {
            beerOrderStatus = BeerOrderStatus.NEW;
        }
    }

    public boolean isReady() {
        return BeerOrderStatus.READY == beerOrderStatus;
    }

    public void calculateTotalPrice() {
        this.totalPrice = items.stream()
                .map(OrderItem::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
