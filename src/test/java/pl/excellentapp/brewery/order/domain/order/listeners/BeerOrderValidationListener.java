package pl.excellentapp.brewery.order.domain.order.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import pl.excellentapp.brewery.model.events.BeerValidationResponse;
import pl.excellentapp.brewery.model.events.OrderValidatedEvent;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class BeerOrderValidationListener {

    public static final UUID BEER_NO_VALIDATE_ID = UUID.fromString("a1d9c5ed-199c-459f-8b86-d9841034a46b");
    public static final UUID BEER_WITHOUT_VALIDATE_RESPONSE_ID = UUID.fromString("6497545c-1f23-41e5-8816-bbd6e97c8fc4");

    private final JmsTemplate jmsTemplate;
    private final String allocateOrderResponseQueueName;

    public BeerOrderValidationListener(JmsTemplate jmsTemplate, @Value("${queue.beer.validate-response}") String allocateOrderResponseQueueName) {
        this.jmsTemplate = jmsTemplate;
        this.allocateOrderResponseQueueName = allocateOrderResponseQueueName;
    }

    @JmsListener(destination = "${queue.beer.validate}")
    public void list(OrderValidatedEvent request) {
        boolean isValid = !request.getBeers().contains(BEER_NO_VALIDATE_ID);
        boolean sendResponse = !Objects.equals(request.getOrderId(), BEER_WITHOUT_VALIDATE_RESPONSE_ID);

        final var responseEvent = BeerValidationResponse.builder()
                .orderId(request.getOrderId())
                .success(isValid);

        if (sendResponse) {
            jmsTemplate.convertAndSend(allocateOrderResponseQueueName, responseEvent.build());
        }
    }
}
