package pl.excellentapp.brewery.order.domain.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    private OrderItem orderItem;
    private UUID beerId;

    @BeforeEach
    void setUp() {
        beerId = UUID.randomUUID();
        orderItem = new OrderItem(beerId, 10);
    }

    @Test
    void testConstructorWithValidData() {
        assertNotNull(orderItem);
        assertEquals(beerId, orderItem.getBeerId());
        assertEquals(10, orderItem.getOrderedQuantity());
        assertEquals(0, orderItem.getReservedQuantity());
        assertEquals(BigDecimal.ZERO, orderItem.getPrice());
    }

    @Test
    void testConstructorWithInvalidQuantity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new OrderItem(beerId, -5);
        });
        assertEquals("Quantity must be a positive number", exception.getMessage());
    }

    @Test
    void testReserved() {
        BigDecimal price = new BigDecimal("5.50");
        orderItem.reserved(3, price);
        assertEquals(3, orderItem.getReservedQuantity());
        assertEquals(price, orderItem.getPrice());
    }

    @Test
    void testGetTotalPrice() {
        orderItem.reserved(3, new BigDecimal("5.50"));
        assertEquals(new BigDecimal("55.00"), orderItem.getTotalPrice());
    }

    @Test
    void testIsFullyReservedWhenNotFullyReserved() {
        orderItem.reserved(3, new BigDecimal("5.50"));
        assertFalse(orderItem.isFullyReserved());
    }

    @Test
    void testIsFullyReservedWhenFullyReserved() {
        orderItem.reserved(10, new BigDecimal("5.50"));
        assertTrue(orderItem.isFullyReserved());
    }

    @Test
    void testIsFullyReservedWhenNoReservation() {
        assertFalse(orderItem.isFullyReserved());
    }
}