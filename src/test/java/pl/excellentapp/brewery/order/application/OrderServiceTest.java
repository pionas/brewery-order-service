package pl.excellentapp.brewery.order.application;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.excellentapp.brewery.order.domain.Order;
import pl.excellentapp.brewery.order.domain.OrderRepository;
import pl.excellentapp.brewery.order.domain.exception.OrderNotFoundException;
import pl.excellentapp.brewery.order.utils.DateTimeProvider;

import java.math.BigDecimal;
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
    private final DateTimeProvider dateTimeProvider = Mockito.mock(DateTimeProvider.class);

    private final OrderService orderService = new OrderServiceImpl(orderRepository, dateTimeProvider);


    @Test
    void findAll_ShouldReturnListOfOrders() {
        // given
        final var orders = List.of(
                createOrder(UUID.fromString("1b4e28ba-2fa1-4d3b-a3f5-ef19b5a7633b"), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("2c4f2ed6-bd1d-4f9d-82c6-6b975b5cf5b3"), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("3a8e0e2f-587d-4b3c-b1c9-27f5d6c3627a"), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("4c9e7a3b-84e7-4f8e-95e2-cd2f1d56e6b7"), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("5d3f8e7c-9f2b-42e1-908d-cf3d1e678e9b"), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("6e8f9d4c-7c8a-45d1-8b4c-ed3f5a7b6e9d"), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("7f1b3c2d-8e9f-41b2-94c8-ef3d7a6b5c9f"), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("8a2d4e6f-9b3c-4e2f-b7d1-2c3f5a8e7b6d"), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("9c3e5d7a-b8f2-41c3-82e9-f2b1d6e5c4f7"), OFFSET_DATE_TIME),
                createOrder(UUID.fromString("0d1e2f3b-5a7c-4d1f-8e9b-2f3d6a8b7c5f"), OFFSET_DATE_TIME)
        );
        when(orderRepository.findAll()).thenReturn(orders);

        // when
        final var result = orderService.findAll();

        // then
        assertEquals(10, result.size());
        assertEquals(orders, result);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnOrder_WhenOrderExists() {
        // given
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), OFFSET_DATE_TIME);
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
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), OFFSET_DATE_TIME);
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
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), OFFSET_DATE_TIME);
        when(orderRepository.save(order)).thenReturn(order);

        // when
        final var result = orderService.create(order);

        // then
        assertEquals(order, result);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void update_ShouldUpdateAndReturnOrder() {
        // given
        OffsetDateTime offsetDateTime = OffsetDateTime.of(2025, 1, 23, 12, 7, 10, 0, ZoneOffset.UTC);
        final var originalOrder = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), OFFSET_DATE_TIME);
        final var updateRequest = getUpdateRequest(originalOrder);
        final var expectedOrder = getExpectedOrder(updateRequest, offsetDateTime);

        when(orderRepository.findById(originalOrder.getId())).thenReturn(Optional.of(originalOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dateTimeProvider.now()).thenReturn(offsetDateTime);

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
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), OFFSET_DATE_TIME);
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
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"), OFFSET_DATE_TIME);
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
                OFFSET_DATE_TIME
        );
    }

    private Order getExpectedOrder(Order originalOrder, OffsetDateTime offsetDateTime) {
        return createOrder(
                originalOrder.getId(),
                offsetDateTime
        );
    }

    private Order createOrder(UUID id, OffsetDateTime offsetDateTime) {
        return Order.builder()
                .id(id)
                .build();
    }
}