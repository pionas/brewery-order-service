package pl.excellentapp.brewery.order.infrastructure.service.beercustomer;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.web.client.RestTemplate;
import pl.excellentapp.brewery.order.domain.beercustomer.BeerCustomer;
import pl.excellentapp.brewery.order.domain.beercustomer.BeerCustomerService;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
class RestBeerCustomerClient implements BeerCustomerService {

    private static final String CUSTOMER_ID = "/{beerId}";
    private final RestTemplate restTemplate;
    private final String url;

    @Override
    public Optional<BeerCustomer> getCustomer(@NonNull UUID customerId) {
        return Optional.ofNullable(restTemplate.getForObject(url + CUSTOMER_ID, BeerCustomerResponse.class, customerId))
                .map(BeerCustomerResponse::toBeerCustomer);
    }
}
