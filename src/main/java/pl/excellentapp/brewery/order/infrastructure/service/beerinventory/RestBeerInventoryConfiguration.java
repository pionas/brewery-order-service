package pl.excellentapp.brewery.order.infrastructure.service.beerinventory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventoryService;

@Configuration(proxyBeanMethods = false)
class RestBeerInventoryConfiguration {

    @Bean
    BeerInventoryService beerInventoryService() {
        return new RestBeerInventoryClient();
    }

}
