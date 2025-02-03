package pl.excellentapp.brewery.order.domain.statemachine.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.excellentapp.brewery.model.events.OrderValidatedEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;

import java.util.UUID;

@Slf4j
@Component
public class ValidateOrderAction extends AbstractAction {

    private final OrderRepository beerOrderRepository;
    private final JmsTemplate jmsTemplate;
    private final String validateOrderQueueName;

    public ValidateOrderAction(OrderRepository beerOrderRepository, JmsTemplate jmsTemplate, @Value("${queue.order.validate}") String validateOrderQueueName) {
        this.beerOrderRepository = beerOrderRepository;
        this.jmsTemplate = jmsTemplate;
        this.validateOrderQueueName = validateOrderQueueName;
    }

    @Override
    @Transactional
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> context) {
        final var beerOrderId = (String) context.getMessageHeader(BeerOrderManager.BEER_ORDER_ID_HEADER);

        beerOrderRepository.findById(UUID.fromString(beerOrderId))
                .ifPresentOrElse(beerOrder -> {
                    log.debug("Sent Validation request to queue for order id {}", beerOrderId);
                    jmsTemplate.convertAndSend(validateOrderQueueName, new OrderValidatedEvent(beerOrder.getId()));
                }, () -> log.error("Order Not Found. Id: {}", beerOrderId));
    }
}
