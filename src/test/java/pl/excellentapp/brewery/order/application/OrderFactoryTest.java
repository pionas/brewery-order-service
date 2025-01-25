package pl.excellentapp.brewery.order.application;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventory;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventoryService;
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.domain.order.OrderStatus;
import pl.excellentapp.brewery.order.utils.DateTimeProvider;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderFactoryTest {
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(2025, 1, 23, 12, 7, 0, 0, ZoneOffset.UTC);

    private final BeerInventoryService beerInventoryService = Mockito.mock(BeerInventoryService.class);
    private final DateTimeProvider dateTimeProvider = Mockito.mock(DateTimeProvider.class);

    private final OrderFactory orderFactory = new OrderFactoryImpl(beerInventoryService, dateTimeProvider);

    @Test
    void testCreateOrderWithSufficientInventory() {
        // given
        final var customerId = UUID.randomUUID();
        final var beerInventory1 = new BeerInventory(UUID.randomUUID(), "Beer1", 10, BigDecimal.valueOf(10));
        final var beerInventory2 = new BeerInventory(UUID.randomUUID(), "Beer2", 5, BigDecimal.valueOf(15));
        final var item1 = new OrderItem(beerInventory1.getId(), 5);
        final var item2 = new OrderItem(beerInventory2.getId(), 3);
        when(beerInventoryService.getInventory(item1.getBeerId())).thenReturn(beerInventory1);
        when(beerInventoryService.getInventory(item2.getBeerId())).thenReturn(beerInventory2);
        when(dateTimeProvider.now()).thenReturn(OFFSET_DATE_TIME);

        // when
        final var order = orderFactory.createOrder(customerId, Arrays.asList(item1, item2));

        // then
        assertNotNull(order);
        assertEquals(customerId, order.getCustomerId());
        assertEquals(OrderStatus.READY, order.getOrderStatus());
        assertEquals(BigDecimal.valueOf(95), order.getTotalPrice());
        assertEquals(2, order.getItems().size());
        assertTrue(item1.isFullyReserved());
        assertTrue(item2.isFullyReserved());
    }

    @Test
    void testCreateOrderWithInsufficientInventory() {
        // given
        final var customerId = UUID.randomUUID();
        final var beerInventory1 = new BeerInventory(UUID.randomUUID(), "Beer1", 2, BigDecimal.valueOf(10));
        final var item1 = new OrderItem(beerInventory1.getId(), 5);
        when(beerInventoryService.getInventory(item1.getBeerId())).thenReturn(beerInventory1);
        when(dateTimeProvider.now()).thenReturn(OFFSET_DATE_TIME);

        // when
        final var order = orderFactory.createOrder(customerId, List.of(item1));

        // then
        assertNotNull(order);
        assertEquals(OrderStatus.NEW, order.getOrderStatus());
        assertEquals(BigDecimal.valueOf(50), order.getTotalPrice());
        assertEquals(1, order.getItems().size());
        assertFalse(item1.isFullyReserved());
        verify(beerInventoryService).getInventory(item1.getBeerId());
    }

    @Test
    void testCreateOrderWithEmptyOrderItems() {
        // given
        final var customerId = UUID.randomUUID();

        // when
        final var order = orderFactory.createOrder(customerId, List.of());

        // then
        assertNotNull(order);
        assertEquals(OrderStatus.NEW, order.getOrderStatus());
        assertEquals(BigDecimal.ZERO, order.getTotalPrice());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void testCreateOrderWithMixedReservationStatus() {
        // given
        final var customerId = UUID.randomUUID();
        final var beerInventory1 = new BeerInventory(UUID.randomUUID(), "Beer1", 10, BigDecimal.valueOf(10));
        final var beerInventory2 = new BeerInventory(UUID.randomUUID(), "Beer2", 3, BigDecimal.valueOf(15));
        final var item1 = new OrderItem(beerInventory1.getId(), 5);
        final var item2 = new OrderItem(beerInventory2.getId(), 5);

        when(beerInventoryService.getInventory(item1.getBeerId())).thenReturn(beerInventory1);
        when(beerInventoryService.getInventory(item2.getBeerId())).thenReturn(beerInventory2);
        when(dateTimeProvider.now()).thenReturn(OFFSET_DATE_TIME);

        // when
        final var order = orderFactory.createOrder(customerId, Arrays.asList(item1, item2));

        // then
        assertNotNull(order);
        assertEquals(OrderStatus.NEW, order.getOrderStatus());
        assertEquals(BigDecimal.valueOf(125), order.getTotalPrice());
        assertFalse(item1.isFullyReserved());
        assertFalse(item2.isFullyReserved());
    }

}