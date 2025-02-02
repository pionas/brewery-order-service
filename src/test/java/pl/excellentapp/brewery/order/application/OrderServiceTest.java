package pl.excellentapp.brewery.order.application;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.excellentapp.brewery.order.domain.exception.OrderNotFoundException;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(2025, 1, 23, 12, 7, 0, 0, ZoneOffset.UTC);
    private static final UUID CUSTOMER_ID = UUID.fromString("4c2ed01e-a2da-4d6b-be10-3c6b10b6a8b4");
    private static final UUID BEER_ID_1 = UUID.fromString("82159174-2449-47b0-aa7f-fa33f8515fad");
    private static final UUID BEER_ID_2 = UUID.fromString("0c99c16c-822e-4495-9bf0-d6f59f3daf12");
    private static final UUID BEER_ID_3 = UUID.fromString("354a3171-2f34-4584-ac61-e0dc1d43161e");

    private final OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
    private final OrderFactory orderFactory = Mockito.mock(OrderFactory.class);
    private final OrderUpdated orderUpdated = Mockito.mock(OrderUpdated.class);
    private final OrderEventPublisher orderEventPublisher = Mockito.mock(OrderEventPublisher.class);

    private final OrderService orderService = new OrderServiceImpl(orderRepository, orderFactory, orderUpdated, orderEventPublisher);


    @Test
    void shouldReturnEmptyList() {
        // given
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        final var result = orderService.findAll();

        // then
        assertEquals(0, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void shouldReturnListOfOrders() {
        // given
        final var customerId1 = UUID.fromString("b78f9a11-cbc0-4f1c-b7c5-16e62cc25732");
        final var customerId2 = UUID.fromString("30079a35-3402-410c-b7ba-b8ad079bb710");
        final var customerId3 = UUID.fromString("054ad93c-b47e-46b7-8fe8-e836b3f1f6f8");
        final var customerId4 = UUID.fromString("7199a48e-e5bb-41e6-bc71-7c27b34aa3e7");
        final var status1 = BeerOrderStatus.NEW;
        final var status2 = BeerOrderStatus.ALLOCATED;
        final var status3 = BeerOrderStatus.CANCELLED_BY_USER;
        final var status4 = BeerOrderStatus.NEW;
        final var orderItem1 = new OrderItem(UUID.randomUUID(), 1);
        final var orderItem2 = new OrderItem(UUID.randomUUID(), 2);
        final var orderItem3 = new OrderItem(UUID.randomUUID(), 5);
        final var orderItem4 = new OrderItem(UUID.randomUUID(), 7);
        final var orderItem5 = new OrderItem(UUID.randomUUID(), 10);
        final var orders = List.of(
                createOrder(UUID.fromString("1b4e28ba-2fa1-4d3b-a3f5-ef19b5a7633b"), customerId1, status1, List.of(orderItem1, orderItem2), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("2c4f2ed6-bd1d-4f9d-82c6-6b975b5cf5b3"), customerId2, status2, List.of(orderItem3), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("3a8e0e2f-587d-4b3c-b1c9-27f5d6c3627a"), customerId3, status3, List.of(orderItem4), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("4c9e7a3b-84e7-4f8e-95e2-cd2f1d56e6b7"), customerId4, status4, List.of(orderItem5), OFFSET_DATE_TIME)
        );
        when(orderRepository.findAll()).thenReturn(orders);

        // when
        final var result = orderService.findAll();

        // then
        assertEquals(4, result.size());
        assertEquals(orders, result);
        verify(orderRepository).findAll();
    }

    @Test
    void shouldReturnOrderById() {
        // given
        final var beerId = UUID.fromString("34a13d2f-b953-4b25-b529-d9f00c1ba41c");
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        final var customerId = UUID.fromString("b78f9a11-cbc0-4f1c-b7c5-16e62cc25732");
        final var status = BeerOrderStatus.NEW;
        final var orderItem = new OrderItem(beerId, 1);
        final var order = createOrder(orderId, customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // when
        final var result = orderService.findById(order.getId());

        // then
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository).findById(order.getId());
    }

    @Test
    void shouldReturnEmptyWhenOrderByIdNotExists() {
        // given
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when
        final var orderOptional = orderService.findById(orderId);

        // then
        assertTrue(orderOptional.isEmpty());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldCreateOrder() {
        // given
        final var customerId = UUID.fromString("b78f9a11-cbc0-4f1c-b7c5-16e62cc25732");
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        final var beerId = UUID.fromString("34a13d2f-b953-4b25-b529-d9f00c1ba41c");
        final var status = BeerOrderStatus.NEW;
        final var orderItem = new OrderItem(beerId, 1);
        final var order = createOrder(orderId, customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        when(orderFactory.createOrder(customerId, List.of(orderItem))).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);

        // when
        final var result = orderService.create(order.getCustomerId(), order.getItems());

        // then
        assertEquals(order, result);
        verify(orderRepository).save(order);
        verify(orderEventPublisher).publishOrderCreatedEvent(order);
    }

    @Test
    void shouldUpdateOrderWithoutProduceReadyEvent() {
        // given
        final var status = BeerOrderStatus.NEW;
        final var item1 = getOrderItem(BEER_ID_1, 11, BigDecimal.valueOf(3.33), 11);
        final var item2 = getOrderItem(BEER_ID_2, 3, BigDecimal.valueOf(4.44), 3);
        final var item3 = getOrderItem(BEER_ID_3, 2, BigDecimal.valueOf(5.55), 2);
        final var originalOrder = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), CUSTOMER_ID, status, List.of(item1), OFFSET_DATE_TIME);

        when(orderRepository.findById(originalOrder.getId())).thenReturn(Optional.of(originalOrder));
        when(orderUpdated.update(any(Order.class), any())).thenReturn(originalOrder);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        final var result = orderService.update(originalOrder.getId(), List.of(item1, item2, item3));

        // then
        assertEquals(originalOrder, result);
        verify(orderRepository).findById(originalOrder.getId());
        verify(orderRepository).save(originalOrder);
        verify(orderEventPublisher, never()).publishOrderReadyEvent(originalOrder);
    }

    @Test
    void shouldUpdateOrderAndProduceReadyEvent() {
        // given
        final var status = BeerOrderStatus.ALLOCATED;
        final var item1 = getOrderItem(BEER_ID_1, 11, BigDecimal.valueOf(3.33), 11);
        final var item2 = getOrderItem(BEER_ID_2, 3, BigDecimal.valueOf(4.44), 3);
        final var item3 = getOrderItem(BEER_ID_3, 2, BigDecimal.valueOf(5.55), 2);
        final var originalOrder = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), CUSTOMER_ID, status, List.of(item1), OFFSET_DATE_TIME);

        when(orderRepository.findById(originalOrder.getId())).thenReturn(Optional.of(originalOrder));
        when(orderUpdated.update(any(Order.class), any())).thenReturn(originalOrder);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        final var result = orderService.update(originalOrder.getId(), List.of(item1, item2, item3));

        // then
        assertEquals(originalOrder, result);
        verify(orderRepository).findById(originalOrder.getId());
        verify(orderRepository).save(originalOrder);
        verify(orderEventPublisher).publishOrderReadyEvent(originalOrder);
    }

    @Test
    void shouldDeleteOrder() {
        // given
        final var status = BeerOrderStatus.NEW;
        final var orderItem = new OrderItem(UUID.randomUUID(), 1);
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), CUSTOMER_ID, status, List.of(orderItem), OFFSET_DATE_TIME);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).deleteById(order.getId());

        // when
        assertDoesNotThrow(() -> orderService.delete(order.getId()));

        // then
        verify(orderRepository).findById(order.getId());
        verify(orderRepository).deleteById(order.getId());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        // given
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when
        assertThrows(OrderNotFoundException.class, () -> orderService.delete(orderId));

        // then
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrowNotFoundOrderWhenTryMarkAsPickedUp() {
        // given
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when
        assertThrows(OrderNotFoundException.class, () -> orderService.markAsPickedUp(orderId));

        // then
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
        verify(orderEventPublisher, never()).publishOrderPickedUpEvent(any());
    }

    @Test
    void shouldMarkAsPickedUpAndProduceEvent() {
        // given
        final var status = BeerOrderStatus.ALLOCATED;
        final var orderItem = new OrderItem(UUID.randomUUID(), 1);
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), CUSTOMER_ID, status, List.of(orderItem), OFFSET_DATE_TIME);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // when
        orderService.markAsPickedUp(order.getId());

        // then
        verify(orderEventPublisher).publishOrderPickedUpEvent(any());
        verify(orderRepository).save(any());
    }

    @Test
    void shouldMarkAsPickedUpWithoutProduceEvent() {
        // given
        final var status = BeerOrderStatus.NEW;
        final var orderItem = new OrderItem(UUID.randomUUID(), 1);
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), CUSTOMER_ID, status, List.of(orderItem), OFFSET_DATE_TIME);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // when
        final var exception = assertThrows(IllegalStateException.class, () -> orderService.markAsPickedUp(order.getId()));

        // then
        assertNotNull(exception);
        assertEquals("Order is not ready for pickup", exception.getMessage());
        verify(orderEventPublisher, never()).publishOrderPickedUpEvent(any());
        verify(orderRepository, never()).save(any());
    }

    private Order createOrder(UUID id, UUID customerId, BeerOrderStatus beerOrderStatus, List<OrderItem> items, OffsetDateTime offsetDateTime) {
        return Order.builder()
                .id(id)
                .customerId(customerId)
                .orderStatus(beerOrderStatus)
                .items(items)
                .orderDateTime(offsetDateTime)
                .build();
    }

    private static OrderItem getOrderItem(UUID beerId, int orderedQuantity, BigDecimal price, Integer reservedQuantity) {
        final var orderItem = new OrderItem(beerId, orderedQuantity);
        orderItem.setPrice(price);
        orderItem.setReservedQuantity(reservedQuantity);
        return orderItem;
    }
}