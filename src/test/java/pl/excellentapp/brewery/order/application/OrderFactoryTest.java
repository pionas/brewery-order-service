package pl.excellentapp.brewery.order.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.excellentapp.brewery.order.domain.beercustomer.BeerCustomer;
import pl.excellentapp.brewery.order.domain.beercustomer.BeerCustomerService;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventory;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventoryService;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import pl.excellentapp.brewery.order.domain.order.OrderItem;
import pl.excellentapp.brewery.order.utils.DateTimeProvider;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderFactoryTest {

    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(2025, 1, 23, 12, 7, 0, 0, ZoneOffset.UTC);
    private static final UUID ORDER_ID = UUID.fromString("d5793977-f2b4-47b5-a2ea-453ee01fdfd0");
    private static final UUID CUSTOMER_ID = UUID.fromString("4c2ed01e-a2da-4d6b-be10-3c6b10b6a8b4");
    private static final UUID BEER_ID_1 = UUID.fromString("82159174-2449-47b0-aa7f-fa33f8515fad");
    private static final UUID BEER_ID_2 = UUID.fromString("0c99c16c-822e-4495-9bf0-d6f59f3daf12");

    private final BeerInventoryService beerInventoryService = Mockito.mock(BeerInventoryService.class);
    private final BeerCustomerService beerCustomerService = Mockito.mock(BeerCustomerService.class);
    private final DateTimeProvider dateTimeProvider = Mockito.mock(DateTimeProvider.class);

    private final OrderFactory orderFactory = new OrderFactoryImpl(beerInventoryService, beerCustomerService, dateTimeProvider);

    @BeforeEach
    void setUp() {
        when(dateTimeProvider.now()).thenReturn(OFFSET_DATE_TIME);
    }

    @Test
    void testCreateOrderWithEmptyOrderItems() {
        // given

        // when
        final var exception = assertThrows(IllegalArgumentException.class, () -> orderFactory.createOrder(CUSTOMER_ID, List.of()));

        // then
        assertNotNull(exception);
        assertEquals("Order items cannot be empty", exception.getMessage());
        verify(beerInventoryService, never()).getInventory(any());
        verify(dateTimeProvider, never()).now();
    }

    @Test
    void testCreateOrderWithExceptionWhenCustomerNotExists() {
        // given
        final var beerInventory1 = new BeerInventory(BEER_ID_1, "Beer1", 2, BigDecimal.valueOf(5));
        final var item1 = new OrderItem(beerInventory1.getId(), 3);
        when(beerCustomerService.getCustomer(CUSTOMER_ID)).thenReturn(Optional.empty());

        // when
        final var exception = assertThrows(RuntimeException.class, () -> orderFactory.createOrder(CUSTOMER_ID, List.of(item1)));

        // then
        assertNotNull(exception);
        assertEquals(String.format("Customer %s not found", CUSTOMER_ID), exception.getMessage());
        verify(beerInventoryService, never()).getInventory(any());
        verify(dateTimeProvider, never()).now();
    }

    @Test
    void testCreateOrderWithSufficientInventory() {
        // given
        final var beerInventory1 = new BeerInventory(BEER_ID_1, "Beer1", 15, BigDecimal.valueOf(9.99));
        final var beerInventory2 = new BeerInventory(BEER_ID_2, "Beer2", 5, BigDecimal.valueOf(4.99));
        final var item1 = new OrderItem(beerInventory1.getId(), 11);
        final var item2 = new OrderItem(beerInventory2.getId(), 3);
        when(beerInventoryService.getInventory(item1.getBeerId())).thenReturn(beerInventory1);
        when(beerInventoryService.getInventory(item2.getBeerId())).thenReturn(beerInventory2);
        when(beerCustomerService.getCustomer(CUSTOMER_ID)).thenReturn(Optional.of(createCustomer()));

        // when
        final var order = orderFactory.createOrder(CUSTOMER_ID, Arrays.asList(item1, item2));

        // then
        assertNotNull(order);
        assertNull(order.getId());
        assertEquals(CUSTOMER_ID, order.getCustomerId());
        assertEquals(OFFSET_DATE_TIME, order.getOrderDateTime());
        assertEquals(BigDecimal.valueOf(124.86), order.getTotalPrice());
        assertEquals(BeerOrderStatus.ALLOCATED, order.getOrderStatus());
        List<OrderItem> orderItems = order.getItems();
        assertEquals(2, orderItems.size());
        OrderItem orderItem1 = orderItems.getFirst();
        assertEquals(BEER_ID_1, orderItem1.getBeerId());
        assertEquals(item1.getOrderedQuantity(), orderItem1.getOrderedQuantity());
        assertEquals(item1.getOrderedQuantity(), orderItem1.getReservedQuantity());
        assertEquals(beerInventory1.getPrice(), orderItem1.getPrice());
        assertTrue(orderItem1.isFullyReserved());
        OrderItem orderItem2 = orderItems.getLast();
        assertEquals(BEER_ID_2, orderItem2.getBeerId());
        assertEquals(item2.getOrderedQuantity(), orderItem2.getOrderedQuantity());
        assertEquals(item2.getOrderedQuantity(), orderItem2.getReservedQuantity());
        assertEquals(beerInventory2.getPrice(), orderItem2.getPrice());
        assertTrue(orderItem2.isFullyReserved());
    }

    @Test
    void testCreateOrderWithInsufficientInventory() {
        // given
        final var beerInventory1 = new BeerInventory(BEER_ID_1, "Beer1", 2, BigDecimal.valueOf(5));
        final var beerInventory2 = new BeerInventory(BEER_ID_2, "Beer2", 0, BigDecimal.valueOf(10));
        final var item1 = new OrderItem(beerInventory1.getId(), 3);
        final var item2 = new OrderItem(beerInventory2.getId(), 1);
        when(beerInventoryService.getInventory(item1.getBeerId())).thenReturn(beerInventory1);
        when(beerInventoryService.getInventory(item2.getBeerId())).thenReturn(beerInventory2);
        when(beerCustomerService.getCustomer(CUSTOMER_ID)).thenReturn(Optional.of(createCustomer()));

        // when
        final var order = orderFactory.createOrder(CUSTOMER_ID, List.of(item1, item2));

        // then
        assertNotNull(order);
        assertNull(order.getId());
        assertEquals(CUSTOMER_ID, order.getCustomerId());
        assertEquals(OFFSET_DATE_TIME, order.getOrderDateTime());
        assertEquals(BigDecimal.valueOf(25), order.getTotalPrice());
        assertEquals(BeerOrderStatus.NEW, order.getOrderStatus());
        List<OrderItem> orderItems = order.getItems();
        assertEquals(2, orderItems.size());
        OrderItem orderItem1 = orderItems.getFirst();
        assertEquals(BEER_ID_1, orderItem1.getBeerId());
        assertEquals(item1.getOrderedQuantity(), orderItem1.getOrderedQuantity());
        assertEquals(beerInventory1.getOnHand(), orderItem1.getReservedQuantity());
        assertEquals(beerInventory1.getPrice(), orderItem1.getPrice());
        assertFalse(orderItem1.isFullyReserved());
        OrderItem orderItem2 = orderItems.getLast();
        assertEquals(BEER_ID_2, orderItem2.getBeerId());
        assertEquals(item2.getOrderedQuantity(), orderItem2.getOrderedQuantity());
        assertEquals(beerInventory2.getOnHand(), orderItem2.getReservedQuantity());
        assertEquals(beerInventory2.getPrice(), orderItem2.getPrice());
        assertFalse(orderItem2.isFullyReserved());
    }

    @Test
    void testCreateOrderWithMixedReservationStatus() {
        // given
        final var beerInventory1 = new BeerInventory(BEER_ID_1, "Beer1", 10, BigDecimal.valueOf(5));
        final var beerInventory2 = new BeerInventory(BEER_ID_2, "Beer2", 5, BigDecimal.valueOf(10));
        final var item1 = new OrderItem(beerInventory1.getId(), 5);
        final var item2 = new OrderItem(beerInventory2.getId(), 6);
        when(beerInventoryService.getInventory(item1.getBeerId())).thenReturn(beerInventory1);
        when(beerInventoryService.getInventory(item2.getBeerId())).thenReturn(beerInventory2);
        when(beerCustomerService.getCustomer(CUSTOMER_ID)).thenReturn(Optional.of(createCustomer()));

        // when
        final var order = orderFactory.createOrder(CUSTOMER_ID, Arrays.asList(item1, item2));

        // then
        assertNotNull(order);
        assertNull(order.getId());
        assertEquals(CUSTOMER_ID, order.getCustomerId());
        assertEquals(OFFSET_DATE_TIME, order.getOrderDateTime());
        assertEquals(BigDecimal.valueOf(85), order.getTotalPrice());
        assertEquals(BeerOrderStatus.NEW, order.getOrderStatus());
        List<OrderItem> orderItems = order.getItems();
        assertEquals(2, orderItems.size());
        OrderItem orderItem1 = orderItems.getFirst();
        assertEquals(BEER_ID_1, orderItem1.getBeerId());
        assertEquals(item1.getOrderedQuantity(), orderItem1.getOrderedQuantity());
        assertEquals(item1.getReservedQuantity(), orderItem1.getReservedQuantity());
        assertEquals(beerInventory1.getPrice(), orderItem1.getPrice());
        assertTrue(orderItem1.isFullyReserved());
        OrderItem orderItem2 = orderItems.getLast();
        assertEquals(BEER_ID_2, orderItem2.getBeerId());
        assertEquals(item2.getOrderedQuantity(), orderItem2.getOrderedQuantity());
        assertEquals(beerInventory2.getOnHand(), orderItem2.getReservedQuantity());
        assertEquals(beerInventory2.getPrice(), orderItem2.getPrice());
        assertFalse(orderItem2.isFullyReserved());
    }

    private BeerCustomer createCustomer() {
        return BeerCustomer.builder()
                .id(CUSTOMER_ID)
                .build();
    }

}