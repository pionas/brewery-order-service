package pl.excellentapp.brewery.order.domain.beercustomer;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface BeerCustomerService {

    Optional<BeerCustomer> getCustomer(@NonNull UUID customerId);
}
