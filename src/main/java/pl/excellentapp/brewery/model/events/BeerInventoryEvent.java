package pl.excellentapp.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class BeerInventoryEvent {

    private UUID orderId;
    private UUID beerId;
    private Integer stock;
}
