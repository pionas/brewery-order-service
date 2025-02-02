package pl.excellentapp.brewery.order.domain.statemachine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import pl.excellentapp.brewery.order.domain.order.BeerOrderEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatus, BeerOrderEvent> {

    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public void preStateChange(State<BeerOrderStatus, BeerOrderEvent> state, Message<BeerOrderEvent> message, Transition<BeerOrderStatus, BeerOrderEvent> transition, StateMachine<BeerOrderStatus, BeerOrderEvent> stateMachine, StateMachine<BeerOrderStatus, BeerOrderEvent> rootStateMachine) {

        Optional.ofNullable(message)
                .map(Message::getHeaders)
                .filter(headers -> headers.containsKey(BeerOrderManager.BEER_ORDER_ID_HEADER))
                .map(messageHeaders -> messageHeaders.get(BeerOrderManager.BEER_ORDER_ID_HEADER, String.class))
                .filter(StringUtils::hasLength)
                .map(UUID::fromString)
                .flatMap(orderRepository::findById)
                .ifPresentOrElse(order -> {
                    log.debug("Saving state for order id: {} Status: {}", order.getId(), state.getId());
                    order.setOrderStatus(state.getId());
                    orderRepository.save(order);
                }, () -> {
                    log.error("Not found Beer Order Id Header or not found Beer Order in DB when try change state for Status {}", state.getId());
                });

    }
}