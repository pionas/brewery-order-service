package pl.excellentapp.brewery.order.application;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import pl.excellentapp.brewery.order.domain.order.BeerOrderEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManagerImpl;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static pl.excellentapp.brewery.order.domain.order.BeerOrderManager.BEER_ORDER_ID_HEADER;

class OrderManagerTest {

    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(2025, 1, 23, 12, 7, 0, 0, ZoneOffset.UTC);

    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> stateMachineFactory = Mockito.mock(StateMachineFactory.class);
    private final OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
    private final OrderFactory orderFactory = Mockito.mock(OrderFactory.class);
    private final OrderUpdated orderUpdated = Mockito.mock(OrderUpdated.class);
    private final StateMachineInterceptorAdapter<BeerOrderStatus, BeerOrderEvent> beerOrderStateChangeInterceptor = Mockito.mock(StateMachineInterceptorAdapter.class);

    private final BeerOrderManager orderService = new BeerOrderManagerImpl(stateMachineFactory, orderRepository, orderFactory, orderUpdated, beerOrderStateChangeInterceptor);

    @Test
    void shouldCreateOrder() {
        // given
        final var customerId = UUID.fromString("b78f9a11-cbc0-4f1c-b7c5-16e62cc25732");
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        final var beerId = UUID.fromString("34a13d2f-b953-4b25-b529-d9f00c1ba41c");
        final var status = BeerOrderStatus.NEW;
        final var orderItem = new OrderItem(beerId, 1);
        final var order = createOrder(orderId, customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        final var stateMachine = Mockito.mock(StateMachine.class);
        final var stateMachineAccessor = Mockito.mock(StateMachineAccessor.class);
        when(orderFactory.createOrder(customerId, List.of(orderItem))).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(stateMachineFactory.getStateMachine(orderId)).thenReturn(stateMachine);
        when(stateMachine.stopReactively()).thenReturn(Mono.empty());
        when(stateMachine.startReactively()).thenReturn(Mono.empty());
        when(stateMachine.getStateMachineAccessor()).thenReturn(stateMachineAccessor);
        when(stateMachine.sendEvent(Mockito.any(Mono.class))).thenReturn(Flux.empty());

        // when
        final var result = orderService.newOrder(order.getCustomerId(), order.getItems());

        // then
        assertEquals(order, result);
        verify(orderRepository).save(order);
        final var captor = ArgumentCaptor.forClass(Mono.class);
        verify(stateMachine).sendEvent(captor.capture());
        Mono<Message<BeerOrderEvent>> capturedMono = captor.getValue();
        Message<BeerOrderEvent> capturedMessage = capturedMono.block();
        assertNotNull(capturedMessage);
        assertEquals(BeerOrderEvent.VALIDATE_ORDER, capturedMessage.getPayload());
        assertEquals(order.getId().toString(), capturedMessage.getHeaders().get(BEER_ORDER_ID_HEADER));
    }

    @Test
    void shouldNotValidationActionWhenOrderByIdNotExists() {
        // given
        final var customerId = UUID.fromString("b78f9a11-cbc0-4f1c-b7c5-16e62cc25732");
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        final var beerId = UUID.fromString("34a13d2f-b953-4b25-b529-d9f00c1ba41c");
        final var status = BeerOrderStatus.NEW;
        final var orderItem = new OrderItem(beerId, 1);
        final var order = createOrder(orderId, customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        final var stateMachine = Mockito.mock(StateMachine.class);
        final var stateMachineAccessor = Mockito.mock(StateMachineAccessor.class);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        when(stateMachineFactory.getStateMachine(orderId)).thenReturn(stateMachine);
        when(stateMachine.stopReactively()).thenReturn(Mono.empty());
        when(stateMachine.startReactively()).thenReturn(Mono.empty());
        when(stateMachine.getStateMachineAccessor()).thenReturn(stateMachineAccessor);
        when(stateMachine.sendEvent(Mockito.any(Mono.class))).thenReturn(Flux.empty());

        // when
        orderService.processValidationResult(order.getId(), true);

        // then
        verify(orderRepository).findById(orderId);
        verify(stateMachine, never()).sendEvent(any(Mono.class));
    }

    @Test
    void shouldValidationPassed() {
        // given
        final var customerId = UUID.fromString("b78f9a11-cbc0-4f1c-b7c5-16e62cc25732");
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        final var beerId = UUID.fromString("34a13d2f-b953-4b25-b529-d9f00c1ba41c");
        final var status = BeerOrderStatus.NEW;
        final var orderItem = new OrderItem(beerId, 1);
        final var order = createOrder(orderId, customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        final var stateMachine = Mockito.mock(StateMachine.class);
        final var stateMachineAccessor = Mockito.mock(StateMachineAccessor.class);
        final var captor = ArgumentCaptor.forClass(Mono.class);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(stateMachineFactory.getStateMachine(orderId)).thenReturn(stateMachine);
        when(stateMachine.stopReactively()).thenReturn(Mono.empty());
        when(stateMachine.startReactively()).thenReturn(Mono.empty());
        when(stateMachine.getStateMachineAccessor()).thenReturn(stateMachineAccessor);
        when(stateMachine.sendEvent(Mockito.any(Mono.class))).thenReturn(Flux.empty());

        // when
        orderService.processValidationResult(order.getId(), true);

        // then
        verify(orderRepository, times(13)).findById(orderId);
        verify(stateMachine, times(2)).sendEvent(captor.capture());
        final var allValues = captor.getAllValues();
        final var validationPassedMessage = (Message<BeerOrderEvent>) allValues.getFirst().block();
        assertNotNull(validationPassedMessage);
        assertEquals(BeerOrderEvent.VALIDATION_PASSED, validationPassedMessage.getPayload());
        assertEquals(order.getId().toString(), validationPassedMessage.getHeaders().get(BEER_ORDER_ID_HEADER));
        final var allocateOrderMessage = (Message<BeerOrderEvent>) allValues.getLast().block();
        assertNotNull(allocateOrderMessage);
        assertEquals(BeerOrderEvent.ALLOCATE_ORDER, allocateOrderMessage.getPayload());
        assertEquals(order.getId().toString(), allocateOrderMessage.getHeaders().get(BEER_ORDER_ID_HEADER));
    }

    @Test
    void shouldValidationFailed() {
        // given
        final var customerId = UUID.fromString("b78f9a11-cbc0-4f1c-b7c5-16e62cc25732");
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        final var beerId = UUID.fromString("34a13d2f-b953-4b25-b529-d9f00c1ba41c");
        final var status = BeerOrderStatus.NEW;
        final var orderItem = new OrderItem(beerId, 1);
        final var order = createOrder(orderId, customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        final var stateMachine = Mockito.mock(StateMachine.class);
        final var stateMachineAccessor = Mockito.mock(StateMachineAccessor.class);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(stateMachineFactory.getStateMachine(orderId)).thenReturn(stateMachine);
        when(stateMachine.stopReactively()).thenReturn(Mono.empty());
        when(stateMachine.startReactively()).thenReturn(Mono.empty());
        when(stateMachine.getStateMachineAccessor()).thenReturn(stateMachineAccessor);
        when(stateMachine.sendEvent(Mockito.any(Mono.class))).thenReturn(Flux.empty());

        // when
        orderService.processValidationResult(order.getId(), false);

        // then
        verify(orderRepository).findById(orderId);
        final var captor = ArgumentCaptor.forClass(Mono.class);
        verify(stateMachine).sendEvent(captor.capture());
        Mono<Message<BeerOrderEvent>> capturedMono = captor.getValue();
        Message<BeerOrderEvent> capturedMessage = capturedMono.block();
        assertNotNull(capturedMessage);
        assertEquals(BeerOrderEvent.VALIDATION_FAILED, capturedMessage.getPayload());
        assertEquals(order.getId().toString(), capturedMessage.getHeaders().get(BEER_ORDER_ID_HEADER));
    }

    // TODO: add more tests

    private Order createOrder(UUID id, UUID customerId, BeerOrderStatus beerOrderStatus, List<OrderItem> items, OffsetDateTime offsetDateTime) {
        return Order.builder()
                .id(id)
                .customerId(customerId)
                .orderStatus(beerOrderStatus)
                .items(items)
                .orderDateTime(offsetDateTime)
                .build();
    }
}