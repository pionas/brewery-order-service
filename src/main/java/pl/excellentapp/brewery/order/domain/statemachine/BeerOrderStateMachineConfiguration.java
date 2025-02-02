package pl.excellentapp.brewery.order.domain.statemachine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import pl.excellentapp.brewery.order.domain.order.BeerOrderEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;

import java.util.EnumSet;

@RequiredArgsConstructor
@EnableStateMachineFactory
@Configuration(proxyBeanMethods = false)
@Slf4j
public class BeerOrderStateMachineConfiguration extends StateMachineConfigurerAdapter<BeerOrderStatus, BeerOrderEvent> {

    private final Action<BeerOrderStatus, BeerOrderEvent> validateOrderAction;
    private final Action<BeerOrderStatus, BeerOrderEvent> allocateOrderAction;
    private final Action<BeerOrderStatus, BeerOrderEvent> validationFailureAction;
    private final Action<BeerOrderStatus, BeerOrderEvent> allocationFailureAction;
    private final Action<BeerOrderStatus, BeerOrderEvent> deallocateOrderAction;
    private final Action<BeerOrderStatus, BeerOrderEvent> userCancelOrderAction;
    private final Action<BeerOrderStatus, BeerOrderEvent> systemCancelOrderAction;
    private final Guard<BeerOrderStatus, BeerOrderEvent> beerOrderIdGuard;

    @Override
    public void configure(StateMachineStateConfigurer<BeerOrderStatus, BeerOrderEvent> states) throws Exception {
        states.withStates()
                .initial(BeerOrderStatus.NEW)
                .states(EnumSet.allOf(BeerOrderStatus.class))
                .end(BeerOrderStatus.PICKED_UP)
                .end(BeerOrderStatus.DELIVERED)
                .end(BeerOrderStatus.CANCELLED_BY_USER)
                .end(BeerOrderStatus.CANCELLED_BY_SYSTEM)
                .end(BeerOrderStatus.DELIVERY_EXCEPTION)
                .end(BeerOrderStatus.VALIDATION_EXCEPTION)
                .end(BeerOrderStatus.ALLOCATION_EXCEPTION);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStatus, BeerOrderEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .guard(beerOrderIdGuard)
                .source(BeerOrderStatus.NEW).target(BeerOrderStatus.VALIDATION_PENDING)
                .event(BeerOrderEvent.VALIDATE_ORDER)
                .action(validateOrderAction)
                .and().withExternal()
                .source(BeerOrderStatus.VALIDATION_PENDING).target(BeerOrderStatus.VALIDATED)
                .event(BeerOrderEvent.VALIDATION_PASSED)
                .and().withExternal()
                .source(BeerOrderStatus.VALIDATION_PENDING).target(BeerOrderStatus.CANCELLED_BY_USER)
                .event(BeerOrderEvent.CANCEL_BY_USER)
                .action(userCancelOrderAction)
                .and().withExternal()
                .source(BeerOrderStatus.VALIDATION_PENDING).target(BeerOrderStatus.CANCELLED_BY_SYSTEM)
                .event(BeerOrderEvent.CANCEL_BY_SYSTEM)
                .action(systemCancelOrderAction)
                .and().withExternal()
                .source(BeerOrderStatus.VALIDATION_PENDING).target(BeerOrderStatus.VALIDATION_EXCEPTION)
                .event(BeerOrderEvent.VALIDATION_FAILED)
                .action(validationFailureAction)
                .and().withExternal()
                .source(BeerOrderStatus.VALIDATED).target(BeerOrderStatus.ALLOCATION_PENDING)
                .event(BeerOrderEvent.ALLOCATE_ORDER)
                .action(allocateOrderAction)
                .and().withExternal()
                .source(BeerOrderStatus.VALIDATED).target(BeerOrderStatus.CANCELLED_BY_USER)
                .event(BeerOrderEvent.CANCEL_BY_USER)
                .action(userCancelOrderAction)
                .and().withExternal()
                .source(BeerOrderStatus.VALIDATED).target(BeerOrderStatus.CANCELLED_BY_SYSTEM)
                .event(BeerOrderEvent.CANCEL_BY_SYSTEM)
                .action(systemCancelOrderAction)
                .and().withExternal()
                .source(BeerOrderStatus.ALLOCATION_PENDING).target(BeerOrderStatus.ALLOCATED)
                .event(BeerOrderEvent.ALLOCATION_SUCCESS)
                .and().withExternal()
                .source(BeerOrderStatus.ALLOCATION_PENDING).target(BeerOrderStatus.ALLOCATION_EXCEPTION)
                .event(BeerOrderEvent.ALLOCATION_FAILED)
                .action(allocationFailureAction)
                .and().withExternal()
                .source(BeerOrderStatus.ALLOCATION_PENDING).target(BeerOrderStatus.CANCELLED_BY_USER)
                .event(BeerOrderEvent.CANCEL_BY_USER)
                .action(userCancelOrderAction)
                .and().withExternal()
                .source(BeerOrderStatus.ALLOCATION_PENDING).target(BeerOrderStatus.CANCELLED_BY_SYSTEM)
                .event(BeerOrderEvent.CANCEL_BY_SYSTEM)
                .action(systemCancelOrderAction)
                .and().withExternal()
                .source(BeerOrderStatus.ALLOCATION_PENDING).target(BeerOrderStatus.PENDING_INVENTORY)
                .event(BeerOrderEvent.ALLOCATION_NO_INVENTORY)
                .and().withExternal()
                .source(BeerOrderStatus.ALLOCATED).target(BeerOrderStatus.PICKED_UP)
                .event(BeerOrderEvent.BEER_ORDER_PICKED_UP)
                .and().withExternal()
                .source(BeerOrderStatus.ALLOCATED).target(BeerOrderStatus.CANCELLED_BY_USER)
                .event(BeerOrderEvent.CANCEL_BY_USER)
                .action(deallocateOrderAction)
                .and().withExternal()
                .source(BeerOrderStatus.ALLOCATED).target(BeerOrderStatus.CANCELLED_BY_SYSTEM)
                .event(BeerOrderEvent.CANCEL_BY_SYSTEM)
                .action(deallocateOrderAction);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<BeerOrderStatus, BeerOrderEvent> config) throws Exception {
        StateMachineListenerAdapter<BeerOrderStatus, BeerOrderEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<BeerOrderStatus, BeerOrderEvent> from, State<BeerOrderStatus, BeerOrderEvent> to) {
                log.info("stateChanged(from: {}, to: {})", from, to);
            }
        };
        config.withConfiguration()
                .listener(adapter);
    }
}