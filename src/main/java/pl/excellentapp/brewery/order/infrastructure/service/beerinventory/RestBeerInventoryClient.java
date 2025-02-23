package pl.excellentapp.brewery.order.infrastructure.service.beerinventory;

import lombok.AllArgsConstructor;
import org.springframework.web.client.RestTemplate;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventory;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventoryService;

import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
class RestBeerInventoryClient implements BeerInventoryService {

    private static final String BEER_ID = "/{beerId}";
    private final RestTemplate restTemplate;

    @Override
    public BeerInventory getInventory(UUID beerId) {
        return Objects.requireNonNull(restTemplate.getForObject(BEER_ID, BeerInventoryResponse.class, beerId))
                .toBeerInventory();
    }
}
