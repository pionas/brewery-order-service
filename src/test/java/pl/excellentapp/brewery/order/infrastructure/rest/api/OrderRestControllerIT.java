package pl.excellentapp.brewery.order.infrastructure.rest.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderPagedList;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderResponse;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderRestControllerIT extends AbstractIT {

    private final String MODEL_API_URL = "/api/v1/orders";

    @AfterEach
    void clearDatabase(@Autowired JdbcTemplate jdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "orders");
    }

    @Test
    void shouldReturnEmptyListOfOrders() {
        // given

        // when
        final var response = restTemplate.getForEntity(MODEL_API_URL, OrderPagedList.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final var responseBody = response.getBody();
        assertNotNull(responseBody);
        final var ordersResponse = responseBody.getContent();
        assertNotNull(ordersResponse);
        assertTrue(ordersResponse.isEmpty());
    }

    @Test
    @Sql({"/db/orders.sql"})
    void shouldReturnListOfOrders() {
        // given

        // when
        final var response = restTemplate.getForEntity(MODEL_API_URL, OrderPagedList.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final var responseBody = response.getBody();
        assertNotNull(responseBody);
        final var ordersResponse = responseBody.getContent();
        assertNotNull(ordersResponse);
        assertFalse(ordersResponse.isEmpty());
    }

    @Test
    @Sql({"/db/orders.sql"})
    void shouldReturnOrderById() {
        // given
        final var orderId = UUID.fromString("11b4e28b-2fa1-4d3b-a3f5-ef19b5a7633b");

        // when
        final var response = restTemplate.getForEntity(MODEL_API_URL + "/{orderId}", OrderResponse.class, orderId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final var responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(orderId, responseBody.getId());
    }

    @Test
    @Sql({"/db/orders.sql"})
    void shouldReturnNotFoundWhenOrderByIdNotExists() {
        // given
        final var orderId = UUID.fromString("be09a70b-8916-41de-9299-998decf259d5");

        // when
        final var response = restTemplate.getForEntity(MODEL_API_URL + "/{orderId}", OrderResponse.class, orderId);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundWhenTryDeleteButOrderByIdNotExists() {
        // given
        final var orderId = UUID.fromString("1b4e28ba-2fa1-4d3b-a3f5-ef19b5a7633b");

        // when
        final var response = restTemplate.exchange(
                MODEL_API_URL + "/{orderId}",
                HttpMethod.DELETE,
                null,
                Map.class,
                orderId
        );

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Sql({"/db/orders.sql"})
    void shouldDeleteOrderById() {
        // given
        final var orderId = UUID.fromString("11b4e28b-2fa1-4d3b-a3f5-ef19b5a7633b");

        // when
        final var response = restTemplate.exchange(
                MODEL_API_URL + "/{orderId}",
                HttpMethod.DELETE,
                null,
                Void.class,
                orderId
        );

        // then
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        final var responseVerify = restTemplate.getForEntity(MODEL_API_URL + "/{orderId}", OrderResponse.class, orderId);
        assertEquals(HttpStatus.NOT_FOUND, responseVerify.getStatusCode());
    }

}