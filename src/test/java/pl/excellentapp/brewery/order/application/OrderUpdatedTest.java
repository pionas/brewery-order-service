package pl.excellentapp.brewery.order.application;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventory;
import pl.excellentapp.brewery.order.domain.beerinventory.BeerInventoryService;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class OrderUpdatedTest {

    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(2025, 1, 23, 12, 7, 0, 0, ZoneOffset.UTC);
    private static final UUID ORDER_ID = UUID.fromString("d5793977-f2b4-47b5-a2ea-453ee01fdfd0");
    private static final UUID CUSTOMER_ID = UUID.fromString("4c2ed01e-a2da-4d6b-be10-3c6b10b6a8b4");
    private static final UUID BEER_ID_1 = UUID.fromString("82159174-2449-47b0-aa7f-fa33f8515fad");
    private static final UUID BEER_ID_2 = UUID.fromString("0c99c16c-822e-4495-9bf0-d6f59f3daf12");
    private static final UUID BEER_ID_3 = UUID.fromString("354a3171-2f34-4584-ac61-e0dc1d43161e");

    private final BeerInventoryService beerInventoryService = Mockito.mock(BeerInventoryService.class);

    private final OrderUpdated orderUpdated = new OrderUpdatedImpl(beerInventoryService);

    @Test
    void shouldUpdateNothing() {
        // given
        final var status = BeerOrderStatus.READY;
        final var beerInventory1 = new BeerInventory(BEER_ID_1, "Beer1", 15, BigDecimal.valueOf(9.99));
        final var beerInventory2 = new BeerInventory(BEER_ID_2, "Beer2", 5, BigDecimal.valueOf(4.99));
        final var beerInventory3 = new BeerInventory(BEER_ID_3, "Beer3", 5, BigDecimal.valueOf(4.99));
        final var item1 = getOrderItem(BEER_ID_1, 11, BigDecimal.valueOf(3.33), 11);
        final var item2 = getOrderItem(BEER_ID_2, 3, BigDecimal.valueOf(4.44), 3);
        final var item3 = getOrderItem(BEER_ID_3, 2, BigDecimal.valueOf(5.55), 2);
        final var order = createOrder(ORDER_ID, CUSTOMER_ID, status, List.of(item1, item2), OFFSET_DATE_TIME);
        when(beerInventoryService.getInventory(item1.getBeerId())).thenReturn(beerInventory1);
        when(beerInventoryService.getInventory(item2.getBeerId())).thenReturn(beerInventory2);
        when(beerInventoryService.getInventory(item3.getBeerId())).thenReturn(beerInventory3);

        // when
        final var updatedOrder = orderUpdated.update(order, Arrays.asList(item1, item2, item3));

        // then
        assertNotNull(updatedOrder);
        assertEquals(ORDER_ID, updatedOrder.getId());
        assertEquals(CUSTOMER_ID, updatedOrder.getCustomerId());
        assertEquals(BigDecimal.valueOf(49.95), updatedOrder.getTotalPrice());
        assertEquals(OFFSET_DATE_TIME, updatedOrder.getOrderDateTime());
        assertEquals(BeerOrderStatus.READY, updatedOrder.getBeerOrderStatus());
        assertEquals(1L, updatedOrder.getVersion());
        assertNull(updatedOrder.getLastModifiedDate());
        final var orderItems = updatedOrder.getItems();
        assertNotNull(orderItems);
        OrderItem orderItem1 = orderItems.getFirst();
        assertEquals(BEER_ID_1, orderItem1.getBeerId());
        assertEquals(item1.getOrderedQuantity(), orderItem1.getOrderedQuantity());
        assertEquals(item1.getPrice(), orderItem1.getPrice());
        assertEquals(item1.getReservedQuantity(), orderItem1.getReservedQuantity());
        OrderItem orderItem2 = orderItems.getLast();
        assertEquals(BEER_ID_2, orderItem2.getBeerId());
        assertEquals(item2.getOrderedQuantity(), orderItem2.getOrderedQuantity());
        assertEquals(item2.getPrice(), orderItem2.getPrice());
        assertEquals(item2.getReservedQuantity(), orderItem2.getReservedQuantity());
    }

    @Test
    void shouldUpdateOrderStatusToReady() {
        // given
        final var status = BeerOrderStatus.NEW;
        final var beerInventory1 = new BeerInventory(BEER_ID_1, "Beer1", 15, BigDecimal.valueOf(9.99));
        final var beerInventory2 = new BeerInventory(BEER_ID_2, "Beer2", 5, BigDecimal.valueOf(4.99));
        final var beerInventory3 = new BeerInventory(BEER_ID_3, "Beer3", 5, BigDecimal.valueOf(4.99));
        final var item1 = getOrderItem(BEER_ID_1, 11, BigDecimal.valueOf(3.33), 11);
        final var item2 = getOrderItem(BEER_ID_2, 3, BigDecimal.valueOf(4.44), 3);
        final var item3 = getOrderItem(BEER_ID_3, 2, BigDecimal.valueOf(5.55), 2);
        final var order = createOrder(ORDER_ID, CUSTOMER_ID, status, List.of(item1, item2), OFFSET_DATE_TIME);
        when(beerInventoryService.getInventory(item1.getBeerId())).thenReturn(beerInventory1);
        when(beerInventoryService.getInventory(item2.getBeerId())).thenReturn(beerInventory2);
        when(beerInventoryService.getInventory(item3.getBeerId())).thenReturn(beerInventory3);

        // when
        final var updatedOrder = orderUpdated.update(order, Arrays.asList(item1, item2, item3));

        // then
        assertNotNull(updatedOrder);
        assertEquals(ORDER_ID, updatedOrder.getId());
        assertEquals(CUSTOMER_ID, updatedOrder.getCustomerId());
        assertEquals(BigDecimal.valueOf(49.95), updatedOrder.getTotalPrice());
        assertEquals(OFFSET_DATE_TIME, updatedOrder.getOrderDateTime());
        assertEquals(BeerOrderStatus.READY, updatedOrder.getBeerOrderStatus());
        assertEquals(1L, updatedOrder.getVersion());
        assertNull(updatedOrder.getLastModifiedDate());
        final var orderItems = updatedOrder.getItems();
        assertNotNull(orderItems);
        OrderItem orderItem1 = orderItems.getFirst();
        assertEquals(BEER_ID_1, orderItem1.getBeerId());
        assertEquals(item1.getOrderedQuantity(), orderItem1.getOrderedQuantity());
        assertEquals(item1.getPrice(), orderItem1.getPrice());
        assertEquals(item1.getReservedQuantity(), orderItem1.getReservedQuantity());
        OrderItem orderItem2 = orderItems.getLast();
        assertEquals(BEER_ID_2, orderItem2.getBeerId());
        assertEquals(item2.getOrderedQuantity(), orderItem2.getOrderedQuantity());
        assertEquals(item2.getPrice(), orderItem2.getPrice());
        assertEquals(item2.getReservedQuantity(), orderItem2.getReservedQuantity());
    }

    @Test
    void shouldUpdateFragmentOrderItemReservedQuantity() {
        // given
        final var status = BeerOrderStatus.NEW;
        final var beerInventory1 = new BeerInventory(BEER_ID_1, "Beer1", 3, BigDecimal.valueOf(9.99));
        final var beerInventory2 = new BeerInventory(BEER_ID_2, "Beer2", 1, BigDecimal.valueOf(4.99));
        final var beerInventory3 = new BeerInventory(BEER_ID_3, "Beer3", 1, BigDecimal.valueOf(4.99));
        final var item1 = getOrderItem(BEER_ID_1, 11, BigDecimal.valueOf(3.33), 5);
        final var item2 = getOrderItem(BEER_ID_2, 3, BigDecimal.valueOf(4.44), 1);
        final var item3 = getOrderItem(BEER_ID_3, 2, BigDecimal.valueOf(5.55), 1);
        final var order = createOrder(ORDER_ID, CUSTOMER_ID, status, List.of(item1, item2), OFFSET_DATE_TIME);
        when(beerInventoryService.getInventory(item1.getBeerId())).thenReturn(beerInventory1);
        when(beerInventoryService.getInventory(item2.getBeerId())).thenReturn(beerInventory2);
        when(beerInventoryService.getInventory(item3.getBeerId())).thenReturn(beerInventory3);

        // when
        final var updatedOrder = orderUpdated.update(order, Arrays.asList(item1, item2, item3));

        // then
        assertNotNull(updatedOrder);
        assertEquals(ORDER_ID, updatedOrder.getId());
        assertEquals(CUSTOMER_ID, updatedOrder.getCustomerId());
        assertEquals(BigDecimal.valueOf(49.95), updatedOrder.getTotalPrice());
        assertEquals(OFFSET_DATE_TIME, updatedOrder.getOrderDateTime());
        assertEquals(BeerOrderStatus.NEW, updatedOrder.getBeerOrderStatus());
        assertEquals(1L, updatedOrder.getVersion());
        assertNull(updatedOrder.getLastModifiedDate());
        final var orderItems = updatedOrder.getItems();
        assertNotNull(orderItems);
        OrderItem orderItem1 = orderItems.getFirst();
        assertEquals(BEER_ID_1, orderItem1.getBeerId());
        assertEquals(item1.getOrderedQuantity(), orderItem1.getOrderedQuantity());
        assertEquals(item1.getPrice(), orderItem1.getPrice());
        assertEquals(8, orderItem1.getReservedQuantity());
        OrderItem orderItem2 = orderItems.getLast();
        assertEquals(BEER_ID_2, orderItem2.getBeerId());
        assertEquals(item2.getOrderedQuantity(), orderItem2.getOrderedQuantity());
        assertEquals(item2.getPrice(), orderItem2.getPrice());
        assertEquals(2, orderItem2.getReservedQuantity());
    }

    private static OrderItem getOrderItem(UUID beerId, int orderedQuantity, BigDecimal price, Integer reservedQuantity) {
        final var orderItem = new OrderItem(beerId, orderedQuantity);
        orderItem.setPrice(price);
        orderItem.setReservedQuantity(reservedQuantity);
        return orderItem;
    }

    private Order createOrder(UUID id, UUID customerId, BeerOrderStatus beerOrderStatus, List<OrderItem> items, OffsetDateTime offsetDateTime) {
        return Order.builder()
                .id(id)
                .version(1L)
                .customerId(customerId)
                .beerOrderStatus(beerOrderStatus)
                .items(items)
                .orderDateTime(offsetDateTime)
                .build();
    }

}