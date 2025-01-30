package pl.excellentapp.brewery.order.infrastructure.service.beerinventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventory;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class BeerInventoryResponse {

    private UUID id;
    private String name;
    private Integer onHand;
    private BigDecimal price;

    public BeerInventory toBeerInventory() {
        return BeerInventory.builder()
                .id(id)
                .name(name)
                .onHand(onHand)
                .price(price)
                .build();
    }
}
