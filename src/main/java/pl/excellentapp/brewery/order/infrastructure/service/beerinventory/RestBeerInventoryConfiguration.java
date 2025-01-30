package pl.excellentapp.brewery.order.infrastructure.service.beerinventory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventoryService;

@Configuration(proxyBeanMethods = false)
class RestBeerInventoryConfiguration {

    @Bean
    BeerInventoryService beerInventoryService(RestTemplateBuilder restTemplateBuilder, @Value("${rest.inventory.service}") String url) {
        return new RestBeerInventoryClient(restTemplateBuilder.build(), url);
    }

}
