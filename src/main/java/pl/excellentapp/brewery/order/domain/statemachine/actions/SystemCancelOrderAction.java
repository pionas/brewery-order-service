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
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;

import java.util.UUID;

@Slf4j
@Component
class SystemCancelOrderAction extends AbstractAction {

    private final OrderRepository beerOrderRepository;
    private final JmsTemplate jmsTemplate;
    private final String deallocateOrderQueueName;

    public SystemCancelOrderAction(OrderRepository beerOrderRepository, JmsTemplate jmsTemplate, @Value("${queue.order.deallocate}") String deallocateOrderQueueName) {
        this.beerOrderRepository = beerOrderRepository;
        this.jmsTemplate = jmsTemplate;
        this.deallocateOrderQueueName = deallocateOrderQueueName;
    }

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> context) {
        final var beerOrderId = (String) context.getMessageHeader(BeerOrderManager.BEER_ORDER_ID_HEADER);

        beerOrderRepository.findById(UUID.fromString(beerOrderId))
                .ifPresentOrElse(beerOrder -> {
                    log.info("Order {} was cancellation by system", beerOrderId);
                    changeBeersStock(beerOrder);
//                    notifyUser(beerOrderId); // TODO
                }, () -> log.error("Beer Order Not Found!"));
    }

    private void changeBeersStock(Order beerOrder) {
        beerOrder.getItems()
                .forEach(orderItem -> changeBeerStock(beerOrder.getId(), orderItem));
    }

    private void changeBeerStock(UUID orderId, OrderItem orderItem) {
        jmsTemplate.convertAndSend(deallocateOrderQueueName, new BeerInventoryEvent(orderId, orderItem.getBeerId(), orderItem.getReservedQuantity()));
    }
}
