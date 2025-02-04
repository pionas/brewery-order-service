package pl.excellentapp.brewery.order.domain.beercustomer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class BeerCustomer {

    private UUID id;
    private String firstName;
    private String lastName;
    private String companyName;
    private String email;
}
