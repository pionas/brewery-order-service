package pl.excellentapp.brewery.order.domain.order.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import pl.excellentapp.brewery.model.events.BeerInventoryEvent;
import pl.excellentapp.brewery.model.events.BeerInventoryEventResponse;

import java.util.Objects;
import java.util.UUID;

import static pl.excellentapp.brewery.order.domain.order.listeners.BeerOrderValidationListener.BEER_CHANGE_QUANTITY;

@Slf4j
@Component
public class BeerInventoryAllocateStockListener {

    public static final UUID ORDER_WITHOUT_VALIDATE_RESPONSE_ID = UUID.fromString("6497545c-1f23-41e5-8816-bbd6e97c8fc4");
    public static final int ORDER_INVALID_QUANTITY = 100;

    private final JmsTemplate jmsTemplate;
    private final String allocateOrderResponseQueueName;

    public BeerInventoryAllocateStockListener(JmsTemplate jmsTemplate, @Value("${queue.inventory.allocate-response}") String allocateOrderResponseQueueName) {
        this.jmsTemplate = jmsTemplate;
        this.allocateOrderResponseQueueName = allocateOrderResponseQueueName;
    }

    @JmsListener(destination = "${queue.inventory.allocate-stock}")
    public void list(BeerInventoryEvent request) {
        boolean isValid = true;
        boolean sendResponse = true;


        if (request.getBeers().containsValue(ORDER_INVALID_QUANTITY)) {
            isValid = false;
        } else if (Objects.equals(request.getOrderId(), ORDER_WITHOUT_VALIDATE_RESPONSE_ID)) {
            sendResponse = false;
        }

        if (request.getBeers().containsKey(BEER_CHANGE_QUANTITY)) {
            request.getBeers().put(BEER_CHANGE_QUANTITY, 1);
        }
        final var responseEvent = BeerInventoryEventResponse.builder()
                .orderId(request.getOrderId())
                .beers(request.getBeers())
                .success(isValid);

        if (sendResponse) {
            jmsTemplate.convertAndSend(allocateOrderResponseQueueName, responseEvent.build());
        }
    }
}
