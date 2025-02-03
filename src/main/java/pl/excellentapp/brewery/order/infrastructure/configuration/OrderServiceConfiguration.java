package pl.excellentapp.brewery.order.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.excellentapp.brewery.order.application.OrderEventPublisher;
import pl.excellentapp.brewery.model.events.OrderEventChannel;

@Configuration(proxyBeanMethods = false)
class OrderServiceConfiguration {

    @Bean
    OrderEventPublisher orderEventPublisher(OrderEventChannel eventChannel) {
        return new OrderEventPublisher(eventChannel);
    }
}
