package pl.excellentapp.brewery.order.infrastructure.configuration;

import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;

@Configuration(proxyBeanMethods = false)
class RestTemplateBuilderConfiguration {

    @Bean
    RestTemplateBuilder restTemplateBuilder(RestTemplateBuilderConfigurer configurer, ClientHttpRequestFactory clientHttpRequestFactory) {
        return configurer.configure(
                new RestTemplateBuilder()
                        .requestFactory(() -> clientHttpRequestFactory)
        );
    }
}
