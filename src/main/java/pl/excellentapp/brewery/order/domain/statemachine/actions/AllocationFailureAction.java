package pl.excellentapp.brewery.order.domain.statemachine.actions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;
import pl.excellentapp.brewery.model.events.BeerInventoryEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;

import java.util.UUID;

@Slf4j
@Component
class AllocationFailureAction extends AbstractAction {

    private final OrderRepository beerOrderRepository;
    private final JmsTemplate jmsTemplate;
    private final String deallocateStockOrderQueueName;

    public AllocationFailureAction(OrderRepository beerOrderRepository, JmsTemplate jmsTemplate, @Value("${queue.inventory.deallocate-stock}") String deallocateStockOrderQueueName) {
        this.beerOrderRepository = beerOrderRepository;
        this.jmsTemplate = jmsTemplate;
        this.deallocateStockOrderQueueName = deallocateStockOrderQueueName;
    }

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> context) {
        final var beerOrderId = (String) context.getMessageHeader(BeerOrderManager.BEER_ORDER_ID_HEADER);

        beerOrderRepository.findById(UUID.fromString(beerOrderId))
                .ifPresentOrElse(beerOrder -> {
                    log.error("Sent Allocation Failure Message to queue for order id {}", beerOrderId);
                    changeBeersStock(beerOrder);
                }, () -> log.error("Beer Order Not Found!"));
    }

    private void changeBeersStock(Order beerOrder) {
        jmsTemplate.convertAndSend(deallocateStockOrderQueueName, new BeerInventoryEvent(beerOrder));
    }
}