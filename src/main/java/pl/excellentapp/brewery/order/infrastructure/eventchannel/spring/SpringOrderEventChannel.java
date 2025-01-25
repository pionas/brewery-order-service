package pl.excellentapp.brewery.order.infrastructure.eventchannel.spring;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pl.excellentapp.brewery.order.domain.event.OrderCancelledEvent;
import pl.excellentapp.brewery.order.domain.event.OrderCreatedEvent;
import pl.excellentapp.brewery.order.domain.event.OrderEventChannel;
import pl.excellentapp.brewery.order.domain.event.OrderPickedUpEvent;
import pl.excellentapp.brewery.order.domain.event.OrderReadyEvent;

@Component
@RequiredArgsConstructor
class SpringOrderEventChannel implements OrderEventChannel {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(OrderCreatedEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public void publish(OrderReadyEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public void publish(OrderPickedUpEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public void publish(OrderCancelledEvent event) {
        publisher.publishEvent(event);
    }
}
