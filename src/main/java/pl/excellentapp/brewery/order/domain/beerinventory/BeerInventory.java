package pl.excellentapp.brewery.order.domain.beerinventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class BeerInventory {

    private UUID id;
    private String name;
    private Integer onHand;
    private BigDecimal price;
}
