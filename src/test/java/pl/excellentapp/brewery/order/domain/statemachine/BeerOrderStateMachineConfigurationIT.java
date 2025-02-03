package pl.excellentapp.brewery.order.domain.statemachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import pl.excellentapp.brewery.order.domain.order.BeerOrderEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManagerImpl;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import pl.excellentapp.brewery.order.infrastructure.rest.api.AbstractIT;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class BeerOrderStateMachineConfigurationIT extends AbstractIT {

    @Autowired
    private StateMachineFactory<BeerOrderStatus, BeerOrderEvent> factory;

    private StateMachine<BeerOrderStatus, BeerOrderEvent> stateMachine;

    @BeforeEach
    public void setUp() {
        stateMachine = factory.getStateMachine();
        stateMachine.startReactively().block();
    }

    @Test
    public void testInitialState() {
        // given

        // when

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.NEW);
    }

    @Test
    public void testValidateOrder() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.VALIDATION_PENDING);
    }

    @Test
    public void testValidationPassed() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.VALIDATED);
    }

    @Test
    public void testCancelledByUser() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.CANCEL_BY_USER))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.CANCELLED_BY_USER);
    }

    @Test
    public void testCancelledBySystem() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.CANCEL_BY_SYSTEM))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.CANCELLED_BY_SYSTEM);
    }

    @Test
    public void testValidationException() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_FAILED))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.VALIDATION_EXCEPTION);
    }

    @Test
    public void testAllocationPending() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATE_ORDER))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.ALLOCATION_PENDING);
    }

    @Test
    public void testAllocationCancelledByUser() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.CANCEL_BY_USER))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.CANCELLED_BY_USER);
    }

    @Test
    public void testAllocationCancelledBySystem() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.CANCEL_BY_SYSTEM))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.CANCELLED_BY_SYSTEM);
    }

    @Test
    public void testAllocated() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATION_SUCCESS))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.ALLOCATED);
    }

    @Test
    public void testAllocationException() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATION_FAILED))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.ALLOCATION_EXCEPTION);
    }

    @Test
    public void testAllocatedCancelledByUser() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATION_SUCCESS))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.CANCEL_BY_USER))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.CANCELLED_BY_USER);
    }

    @Test
    public void testAllocatedCancelledBySystem() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATION_SUCCESS))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.CANCEL_BY_SYSTEM))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.CANCELLED_BY_SYSTEM);
    }

    @Test
    public void testAllocationPendingInventory() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATION_NO_INVENTORY))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.PENDING_INVENTORY);
    }

    @Test
    public void testAllocatedPickedUp() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATION_SUCCESS))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.BEER_ORDER_PICKED_UP))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.PICKED_UP);
    }

    @Test
    public void testAllocatedCancelByUser() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATION_SUCCESS))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.CANCEL_BY_USER))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.CANCELLED_BY_USER);
    }

    @Test
    public void testAllocatedCancelBySystem() {
        // given

        // when
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.VALIDATION_PASSED))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATE_ORDER))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.ALLOCATION_SUCCESS))).blockLast();
        stateMachine.sendEvent(Mono.just(getBeerOrderEventMessage(BeerOrderEvent.CANCEL_BY_SYSTEM))).blockLast();

        // then
        assertThat(stateMachine.getState().getId()).isEqualTo(BeerOrderStatus.CANCELLED_BY_SYSTEM);
    }

    private Message<BeerOrderEvent> getBeerOrderEventMessage(BeerOrderEvent beerOrderEvent) {
        return MessageBuilder.withPayload(beerOrderEvent)
                .setHeader(BeerOrderManagerImpl.BEER_ORDER_ID_HEADER, "11b4e28b-2fa1-4d3b-a3f5-ef19b5a7633b")
                .build();
    }
}