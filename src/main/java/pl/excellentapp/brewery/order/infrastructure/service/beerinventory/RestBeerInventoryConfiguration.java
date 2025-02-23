package pl.excellentapp.brewery.order.infrastructure.service.beerinventory;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventoryService;

@Configuration(proxyBeanMethods = false)
class RestBeerInventoryConfiguration {

    @Bean
    BeerInventoryService beerInventoryService(RestTemplateBuilder restTemplateBuilder,
                                              HttpBeerInventoryClientProperties httpBeerInventoryClientProperties) {
        return new RestBeerInventoryClient(prepareRestTemplate(restTemplateBuilder, httpBeerInventoryClientProperties));
    }

    private RestTemplate prepareRestTemplate(RestTemplateBuilder restTemplateBuilder, HttpBeerInventoryClientProperties httpBeerInventoryClientProperties) {
        if (!httpBeerInventoryClientProperties.shouldAuth()) {
            return restTemplateBuilder
                    .rootUri(httpBeerInventoryClientProperties.getUrl())
                    .build();
        }
        return restTemplateBuilder
                .basicAuthentication(httpBeerInventoryClientProperties.getUsername(), httpBeerInventoryClientProperties.getPassword())
                .rootUri(httpBeerInventoryClientProperties.getUrl())
                .build();
    }

}
