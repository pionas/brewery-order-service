package pl.excellentapp.brewery.order.domain.statemachine.actions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;
import pl.excellentapp.brewery.model.events.OrderAllocatedEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;

import java.util.UUID;

@Slf4j
@Component
class AllocateOrderAction extends AbstractAction {

    private final OrderRepository beerOrderRepository;
    private final JmsTemplate jmsTemplate;
    private final String allocateOrderQueueName;

    public AllocateOrderAction(OrderRepository beerOrderRepository, JmsTemplate jmsTemplate, @Value("${queue.order.allocate}") String allocateOrderQueueName) {
        this.beerOrderRepository = beerOrderRepository;
        this.jmsTemplate = jmsTemplate;
        this.allocateOrderQueueName = allocateOrderQueueName;
    }

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> context) {
        final var beerOrderId = (String) context.getMessageHeader(BeerOrderManager.BEER_ORDER_ID_HEADER);

        beerOrderRepository.findById(UUID.fromString(beerOrderId))
                .ifPresentOrElse(beerOrder -> {
                    log.debug("Sent Allocation Request for order id: {}", beerOrderId);
                    jmsTemplate.convertAndSend(allocateOrderQueueName, new OrderAllocatedEvent(beerOrder.getId()));
                }, () -> log.error("Beer Order Not Found!"));
    }
}
