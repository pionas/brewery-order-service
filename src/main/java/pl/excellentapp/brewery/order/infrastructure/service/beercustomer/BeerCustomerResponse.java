package pl.excellentapp.brewery.order.infrastructure.service.beercustomer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.excellentapp.brewery.order.domain.beercustomer.BeerCustomer;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class BeerCustomerResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String companyName;
    private String email;

    public BeerCustomer toBeerCustomer() {
        return BeerCustomer.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .companyName(companyName)
                .email(email)
                .build();
    }
}
