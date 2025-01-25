package pl.excellentapp.brewery.order.application;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.excellentapp.brewery.order.domain.exception.OrderNotFoundException;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;
import pl.excellentapp.brewery.order.domain.order.OrderStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(2025, 1, 23, 12, 7, 0, 0, ZoneOffset.UTC);

    private final OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
    private final OrderFactory orderFactory = Mockito.mock(OrderFactory.class);
    private final OrderEventPublisher orderEventPublisher = Mockito.mock(OrderEventPublisher.class);

    private final OrderService orderService = new OrderServiceImpl(orderRepository, orderFactory, orderEventPublisher);


    @Test
    void findAll_ShouldReturnListOfOrders() {
        // given
        final var customerId1 = UUID.randomUUID();
        final var customerId2 = UUID.randomUUID();
        final var customerId3 = UUID.randomUUID();
        final var customerId4 = UUID.randomUUID();
        final var status1 = OrderStatus.NEW;
        final var status2 = OrderStatus.READY;
        final var status3 = OrderStatus.CANCELLED;
        final var status4 = OrderStatus.NEW;
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
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnOrder_WhenOrderExists() {
        // given
        final var customerId = UUID.randomUUID();
        final var status = OrderStatus.NEW;
        final var orderItem = new OrderItem(UUID.randomUUID(), 1);
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // when
        final var result = orderService.findById(order.getId());

        // then
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository, times(1)).findById(order.getId());
    }

    @Test
    void findById_ShouldThrowException_WhenOrderNotFound() {
        // given
        final var customerId = UUID.randomUUID();
        final var status = OrderStatus.NEW;
        final var orderItem = new OrderItem(UUID.randomUUID(), 1);
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.empty());

        // when
        final var orderOptional = orderService.findById(order.getId());

        // then
        assertTrue(orderOptional.isEmpty());
        verify(orderRepository, times(1)).findById(order.getId());
    }

    @Test
    void create_ShouldSaveAndReturnOrder() {
        // given
        final var customerId = UUID.randomUUID();
        final var status = OrderStatus.NEW;
        final var orderItem = new OrderItem(UUID.randomUUID(), 1);
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        when(orderRepository.save(order)).thenReturn(order);

        // when
        final var result = orderService.create(order.getCustomerId(), order.getItems());

        // then
        assertEquals(order, result);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void update_ShouldUpdateAndReturnOrder() {
        // given
        final var offsetDateTime = OffsetDateTime.of(2025, 1, 23, 12, 7, 10, 0, ZoneOffset.UTC);
        final var customerId = UUID.randomUUID();
        final var status = OrderStatus.NEW;
        final var orderItem = new OrderItem(UUID.randomUUID(), 1);
        final var originalOrder = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        final var updateRequest = getUpdateRequest(originalOrder);
        final var expectedOrder = getExpectedOrder(updateRequest, offsetDateTime);

        when(orderRepository.findById(originalOrder.getId())).thenReturn(Optional.of(originalOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        final var result = orderService.update(originalOrder.getId(), updateRequest);

        // then
        assertEquals(expectedOrder, result);
        assertEquals(expectedOrder.getId(), result.getId());
        verify(orderRepository, times(1)).findById(originalOrder.getId());
        verify(orderRepository, times(1)).save(originalOrder);
    }


    @Test
    void delete_ShouldDeleteOrder_WhenOrderExists() {
        // given
        final var customerId = UUID.randomUUID();
        final var status = OrderStatus.NEW;
        final var orderItem = new OrderItem(UUID.randomUUID(), 1);
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).deleteById(order.getId());

        // when
        assertDoesNotThrow(() -> orderService.delete(order.getId()));

        // then
        verify(orderRepository, times(1)).findById(order.getId());
        verify(orderRepository, times(1)).deleteById(order.getId());
    }

    @Test
    void delete_ShouldThrowException_WhenOrderNotFound() {
        // given
        final var customerId = UUID.randomUUID();
        final var status = OrderStatus.NEW;
        final var orderItem = new OrderItem(UUID.randomUUID(), 1);
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), customerId, status, List.of(orderItem), OFFSET_DATE_TIME);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.empty());

        // when
        assertThrows(OrderNotFoundException.class, () -> orderService.delete(order.getId()));

        // then
        verify(orderRepository, times(1)).findById(order.getId());
        verify(orderRepository, never()).deleteById(any());
    }

    private Order getUpdateRequest(Order originalOrder) {
        return createOrder(
                originalOrder.getId(),
                originalOrder.getCustomerId(),
                originalOrder.getOrderStatus(),
                originalOrder.getItems(),
                OFFSET_DATE_TIME
        );
    }

    private Order getExpectedOrder(Order originalOrder, OffsetDateTime offsetDateTime) {
        return createOrder(
                originalOrder.getId(),
                originalOrder.getCustomerId(),
                originalOrder.getOrderStatus(),
                originalOrder.getItems(),
                offsetDateTime
        );
    }

    private Order createOrder(UUID id, UUID customerId, OrderStatus orderStatus, List<OrderItem> items, OffsetDateTime offsetDateTime) {
        return Order.builder()
                .id(id)
                .customerId(customerId)
                .orderStatus(orderStatus)
                .items(items)
                .orderDateTime(offsetDateTime)
                .build();
    }
}