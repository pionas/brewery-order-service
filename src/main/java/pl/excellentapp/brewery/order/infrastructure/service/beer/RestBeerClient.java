package pl.excellentapp.brewery.order.infrastructure.service.beer;

import lombok.AllArgsConstructor;
import org.springframework.web.client.RestTemplate;
import pl.excellentapp.brewery.model.BeerDto;
import pl.excellentapp.brewery.order.domain.beer.BeerService;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
class RestBeerClient implements BeerService {

    private static final String BEER_ID = "/{beerId}";
    private static final String BEER_UPC = "/upc/{beerId}";
    private final RestTemplate restTemplate;
    private final String url;

    @Override
    public Optional<BeerDto> getBeerById(UUID beerId) {
        return Optional.ofNullable(restTemplate.getForObject(url + BEER_ID, BeerDto.class, beerId));
    }

    @Override
    public Optional<BeerDto> getBeerByUpc(String upc) {
        return Optional.ofNullable(restTemplate.getForObject(url + BEER_UPC, BeerDto.class, upc));
    }
}
