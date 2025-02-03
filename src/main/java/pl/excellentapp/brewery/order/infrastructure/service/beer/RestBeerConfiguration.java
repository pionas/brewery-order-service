package pl.excellentapp.brewery.order.infrastructure.service.beer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.excellentapp.brewery.order.domain.beer.BeerService;

@Configuration(proxyBeanMethods = false)
class RestBeerConfiguration {

    @Bean
    BeerService beerService(RestTemplateBuilder restTemplateBuilder, @Value("${rest.beer.service}") String url) {
        return new RestBeerClient(restTemplateBuilder.build(), url);
    }

}
