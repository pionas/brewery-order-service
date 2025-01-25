package pl.excellentapp.brewery.order.domain.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    private static final UUID BEER_ID = UUID.fromString("91fcf253-1414-4a4f-a733-f726928dbe5c");

    @Test
    void shouldInitializeCorrectly() {
        // given
        final var orderItem = new OrderItem(BEER_ID, 10);

        // when

        // then
        assertNotNull(orderItem);
        assertEquals(BEER_ID, orderItem.getBeerId());
        assertEquals(10, orderItem.getOrderedQuantity());
        assertEquals(0, orderItem.getReservedQuantity());
        assertEquals(BigDecimal.ZERO, orderItem.getPrice());
    }

    @Test
    void shouldThrowExceptionForNonPositiveQuantity() {
        // given

        // when
        final var exception1 = assertThrows(IllegalArgumentException.class, () -> new OrderItem(BEER_ID, -5));
        final var exception2 = assertThrows(IllegalArgumentException.class, () -> new OrderItem(BEER_ID, 0));

        // then
        assertEquals("Quantity must be a positive number", exception1.getMessage());
        assertEquals("Quantity must be a positive number", exception2.getMessage());
    }

    @Test
    void shouldUpdateReserved() {
        // given
        final var orderItem = new OrderItem(BEER_ID, 10);

        // when
        orderItem.reserve(3);

        // then
        assertEquals(3, orderItem.getReservedQuantity());

        // when
        orderItem.reserve(2);

        // then
        assertEquals(5, orderItem.getReservedQuantity());
    }

    @Test
    void shouldNotExceedOrderedQuantity() {
        // given
        final var orderItem = new OrderItem(BEER_ID, 10);

        // when
        orderItem.reserve(15);

        // then
        assertEquals(10, orderItem.getReservedQuantity());
    }

    @Test
    void shouldThrowExceptionForInvalidAvailableQuantity() {
        // given
        final var orderItem = new OrderItem(BEER_ID, 10);

        // when
        assertThrows(IllegalArgumentException.class, () -> orderItem.reserve(null));
        assertThrows(IllegalArgumentException.class, () -> orderItem.reserve(-5));
    }


    @Test
    void shouldUpdatePriceCorrectly() {
        // given
        final var orderItem = new OrderItem(BEER_ID, 10);
        final var price = new BigDecimal("15.50");

        // when
        orderItem.updatePrice(price);

        // then
        assertEquals(price, orderItem.getPrice());
    }

    @Test
    void shouldThrowExceptionForNullPrice() {
        // given
        final var orderItem = new OrderItem(BEER_ID, 10);

        // when
        assertThrows(NullPointerException.class, () -> orderItem.updatePrice(null));
    }

    @Test
    void shouldThrowExceptionForNegativePrice() {
        // given
        final var orderItem = new OrderItem(BEER_ID, 10);

        // when
        final var exception = assertThrows(IllegalArgumentException.class, () -> orderItem.updatePrice(BigDecimal.valueOf(-1)));

        // then
        assertEquals("Price must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForZeroPrice() {
        // given
        final var orderItem = new OrderItem(BEER_ID, 10);

        // when
        final var exception = assertThrows(IllegalArgumentException.class, () -> orderItem.updatePrice(BigDecimal.ZERO));

        // then
        assertEquals("Price must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldReturnCorrectlyCalculateTotalPrice() {
        // given
        final var orderItem = new OrderItem(BEER_ID, 10);
        final var expectedTotal = new BigDecimal("200.00");

        // when
        orderItem.updatePrice(new BigDecimal("20.00"));

        // then
        assertEquals(expectedTotal, orderItem.calculateTotalPrice());
    }

    @Test
    void shouldReturnTrueIfIsFullyReserved() {
        // given
        final var orderItem = new OrderItem(BEER_ID, 10);

        // when
        orderItem.reserve(10);

        // then
        assertTrue(orderItem.isFullyReserved());
    }

    @Test
    void shouldReturnFalseIfIsNotFullyReserved() {
        // given
        final var orderItem = new OrderItem(BEER_ID, 10);

        // when
        orderItem.reserve(5);

        // then
        assertFalse(orderItem.isFullyReserved());
    }

}