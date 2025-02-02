package pl.excellentapp.brewery.order.domain.statemachine.actions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import pl.excellentapp.brewery.order.domain.order.BeerOrderEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class AbstractAction implements Action<BeerOrderStatus, BeerOrderEvent> {

    void sendEvent(StateContext<BeerOrderStatus, BeerOrderEvent> context, BeerOrderEvent event) {
        context.getStateMachine().sendEvent(getMessageMono(context, event))
                .blockLast();
    }

    private Mono<Message<BeerOrderEvent>> getMessageMono(StateContext<BeerOrderStatus, BeerOrderEvent> context, BeerOrderEvent event) {
        return Mono.just(getBeerOrderEventMessage(context, event));
    }

    private Message<BeerOrderEvent> getBeerOrderEventMessage(StateContext<BeerOrderStatus, BeerOrderEvent> context, BeerOrderEvent event) {
        return MessageBuilder.withPayload(event)
                .setHeader(BeerOrderManager.BEER_ORDER_ID_HEADER, context.getMessageHeader(BeerOrderManager.BEER_ORDER_ID_HEADER))
                .build();
    }
}
