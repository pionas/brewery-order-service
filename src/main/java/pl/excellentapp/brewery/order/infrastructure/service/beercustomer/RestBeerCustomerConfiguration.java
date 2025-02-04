package pl.excellentapp.brewery.order.infrastructure.service.beercustomer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.excellentapp.brewery.order.domain.beercustomer.BeerCustomerService;

@Configuration(proxyBeanMethods = false)
class RestBeerCustomerConfiguration {

    @Bean
    BeerCustomerService beerCustomerService(RestTemplateBuilder restTemplateBuilder, @Value("${rest.customer.service}") String url) {
        return new RestBeerCustomerClient(restTemplateBuilder.build(), url);
    }

}
