package pl.excellentapp.brewery.order.domain.beerinventory;

import java.util.UUID;

public interface BeerInventoryService {

    BeerInventory getInventory(UUID beerId);
}
