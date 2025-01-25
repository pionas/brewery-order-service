package pl.excellentapp.brewery.order.infrastructure.service.beerinventory;

import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventory;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventoryService;

import java.util.UUID;

class RestBeerInventoryClient implements BeerInventoryService {

    // TODO: add rest client
    @Override
    public BeerInventory getInventory(UUID beerId) {
        return null;
    }
}
