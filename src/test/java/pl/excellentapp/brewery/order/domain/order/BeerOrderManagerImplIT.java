package pl.excellentapp.brewery.order.domain.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import pl.excellentapp.brewery.model.BeerDto;
import pl.excellentapp.brewery.model.events.BeerInventoryEvent;
import pl.excellentapp.brewery.order.domain.beercustomer.BeerCustomer;
import pl.excellentapp.brewery.order.infrastructure.rest.api.AbstractIT;
import pl.excellentapp.brewery.order.infrastructure.service.beerinventory.BeerInventoryResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pl.excellentapp.brewery.order.domain.order.listeners.BeerInventoryAllocateStockListener.ORDER_INVALID_QUANTITY;
import static pl.excellentapp.brewery.order.domain.order.listeners.BeerOrderValidationListener.BEER_CHANGE_QUANTITY;
import static pl.excellentapp.brewery.order.domain.order.listeners.BeerOrderValidationListener.BEER_NO_VALIDATE_ID;

class BeerOrderManagerImplIT extends AbstractIT {

    private static final String BEER_INVENTORY_ID = "/api/v1/inventories/";
    private static final String BEER_CUSTOMER_ID = "/api/v1/customers/";

    @Autowired
    private BeerOrderManager orderManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    WireMockServer wireMockServer;

    @Value("${queue.inventory.deallocate-stock}")
    private String deallocateStockOrderQueueName;

    @AfterEach
    void afterEach(@Autowired JdbcTemplate jdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "orders_items", "orders");
        wireMockServer.resetAll();
    }

    @Test
    void testNewToAllocated() throws JsonProcessingException {
        // given
        final var orderId = UUID.randomUUID();
        final var customerId = UUID.randomUUID();
        final var beerCustomerResponse = getCustomer(customerId);
        final var beerDto = getBeerDto(UUID.randomUUID());
        final var beerInventoryResponse = getBeerInventoryResponse(beerDto);
        final var order = createOrder(orderId, customerId, beerDto, 1);
        wireMockServer.stubFor(get(BEER_CUSTOMER_ID + customerId)
                .willReturn(okJson(objectMapper.writeValueAsString(beerCustomerResponse))));

        wireMockServer.stubFor(get(BEER_INVENTORY_ID + beerDto.getId())
                .willReturn(okJson(objectMapper.writeValueAsString(beerInventoryResponse))));

        // when
        final var savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        // then
        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();

            assertEquals(BeerOrderStatus.ALLOCATED, foundOrder.getOrderStatus());
        });

        final var savedOrder2 = orderRepository.findById(savedOrder.getId()).get();
        assertNotNull(savedOrder2);
        assertEquals(BeerOrderStatus.ALLOCATED, savedOrder2.getOrderStatus());
        savedOrder2.getItems().forEach(line -> assertEquals(line.getOrderedQuantity(), line.getReservedQuantity()));
    }

    @Test
    void testFailedValidation() throws JsonProcessingException {
        // given
        final var orderId = UUID.randomUUID();
        final var customerId = UUID.randomUUID();
        final var beerCustomerResponse = getCustomer(customerId);
        final var beerDto = getBeerDto(BEER_NO_VALIDATE_ID);
        final var beerInventoryResponse = getBeerInventoryResponse(beerDto);
        final var order = createOrder(orderId, customerId, beerDto, 1);
        wireMockServer.stubFor(get(BEER_CUSTOMER_ID + customerId)
                .willReturn(okJson(objectMapper.writeValueAsString(beerCustomerResponse))));
        wireMockServer.stubFor(get(BEER_INVENTORY_ID + beerDto.getId())
                .willReturn(okJson(objectMapper.writeValueAsString(beerInventoryResponse))));

        // when
        final var savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        // then
        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();

            assertEquals(BeerOrderStatus.VALIDATION_EXCEPTION, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {
        // given
        final var orderId = UUID.randomUUID();
        final var customerId = UUID.randomUUID();
        final var beerCustomerResponse = getCustomer(customerId);
        final var beerDto = getBeerDto(UUID.randomUUID());
        final var beerInventoryResponse = getBeerInventoryResponse(beerDto);
        final var order = createOrder(orderId, customerId, beerDto, 1);
        wireMockServer.stubFor(get(BEER_CUSTOMER_ID + customerId)
                .willReturn(okJson(objectMapper.writeValueAsString(beerCustomerResponse))));
        wireMockServer.stubFor(get(BEER_INVENTORY_ID + beerDto.getId())
                .willReturn(okJson(objectMapper.writeValueAsString(beerInventoryResponse))));

        final var savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());
        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(BeerOrderStatus.ALLOCATED, foundOrder.getOrderStatus());
        });
        // when
        orderManager.orderPickedUp(savedOrder.getId());

        // then
        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(BeerOrderStatus.PICKED_UP, foundOrder.getOrderStatus());
        });
        final var pickedUpOrder = orderRepository.findById(savedOrder.getId()).get();
        assertEquals(BeerOrderStatus.PICKED_UP, pickedUpOrder.getOrderStatus());
    }

    @Test
    void testAllocationFailure() throws JsonProcessingException {
        // given
        final var customerId = UUID.randomUUID();
        final var orderId = UUID.randomUUID();
        final var beerId = UUID.randomUUID();
        final var beerCustomerResponse = getCustomer(customerId);
        final var beerDto = getBeerDto(beerId);
        final var beerInventoryResponse = getBeerInventoryResponse(beerDto);
        final var order = createOrder(orderId, customerId, beerDto, ORDER_INVALID_QUANTITY);
        wireMockServer.stubFor(get(BEER_CUSTOMER_ID + customerId)
                .willReturn(okJson(objectMapper.writeValueAsString(beerCustomerResponse))));
        wireMockServer.stubFor(get(BEER_INVENTORY_ID + beerDto.getId())
                .willReturn(okJson(objectMapper.writeValueAsString(beerInventoryResponse))));

        // when
        final var savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        // then
        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(BeerOrderStatus.ALLOCATION_EXCEPTION, foundOrder.getOrderStatus());
        });

        final var allocationFailureEvent = (BeerInventoryEvent) jmsTemplate.receiveAndConvert(deallocateStockOrderQueueName);
        assertNotNull(allocationFailureEvent);
        assertThat(allocationFailureEvent.getOrderId()).isEqualTo(savedOrder.getId());
    }

    @Test
    void testPartialAllocation() throws JsonProcessingException {
        final var orderId = UUID.randomUUID();
        final var customerId = UUID.randomUUID();
        final var beerDto = getBeerDto(BEER_CHANGE_QUANTITY);
        final var beerCustomerResponse = getCustomer(customerId);
        final var order = createOrder(orderId, customerId, beerDto, 30);
        final var beerInventoryResponse = getBeerInventoryResponse(beerDto);
        wireMockServer.stubFor(get(BEER_CUSTOMER_ID + customerId)
                .willReturn(okJson(objectMapper.writeValueAsString(beerCustomerResponse))));
        wireMockServer.stubFor(get(BEER_INVENTORY_ID + beerDto.getId())
                .willReturn(okJson(objectMapper.writeValueAsString(beerInventoryResponse))));

        // when
        final var savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        // then
        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(BeerOrderStatus.PENDING_INVENTORY, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testValidationPendingToCancel() throws JsonProcessingException {
        // given
        final var orderId = UUID.randomUUID();
        final var customerId = UUID.randomUUID();
        final var beerCustomerResponse = getCustomer(customerId);
        final var beerDto = getBeerDto(UUID.randomUUID());
        final var order = createOrder(orderId, customerId, beerDto, 1);
        final var beerInventoryResponse = getBeerInventoryResponse(beerDto);
        wireMockServer.stubFor(get(BEER_CUSTOMER_ID + customerId)
                .willReturn(okJson(objectMapper.writeValueAsString(beerCustomerResponse))));
        wireMockServer.stubFor(get(BEER_INVENTORY_ID + beerDto.getId())
                .willReturn(okJson(objectMapper.writeValueAsString(beerInventoryResponse))));

        // when
        final var savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        // then
        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(BeerOrderStatus.VALIDATION_PENDING, foundOrder.getOrderStatus());
        });

        orderManager.cancel(savedOrder.getId());
        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(BeerOrderStatus.CANCELLED_BY_SYSTEM, foundOrder.getOrderStatus());
        });
        final var beerInventoryEvent = (BeerInventoryEvent) jmsTemplate.receiveAndConvert(deallocateStockOrderQueueName);
        assertNotNull(beerInventoryEvent);
        assertThat(beerInventoryEvent.getOrderId()).isEqualTo(savedOrder.getId());
    }

    @Test
    void testAllocationPendingToCancel() throws JsonProcessingException {
        // given
        final var orderId = UUID.randomUUID();
        final var customerId = UUID.randomUUID();
        final var beerCustomerResponse = getCustomer(customerId);
        final var beerDto = getBeerDto(UUID.randomUUID());
        final var beerInventoryResponse = getBeerInventoryResponse(beerDto);
        final var order = createOrder(orderId, customerId, beerDto, 1);
        wireMockServer.stubFor(get(BEER_CUSTOMER_ID + customerId)
                .willReturn(okJson(objectMapper.writeValueAsString(beerCustomerResponse))));
        wireMockServer.stubFor(get(BEER_INVENTORY_ID + beerDto.getId())
                .willReturn(okJson(objectMapper.writeValueAsString(beerInventoryResponse))));

        final var savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(BeerOrderStatus.ALLOCATION_PENDING, foundOrder.getOrderStatus());
        });

        // when
        orderManager.cancel(savedOrder.getId());

        // then
        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(BeerOrderStatus.CANCELLED_BY_SYSTEM, foundOrder.getOrderStatus());
        });
        final var beerInventoryEvent = (BeerInventoryEvent) jmsTemplate.receiveAndConvert(deallocateStockOrderQueueName);
        assertNotNull(beerInventoryEvent);
        assertThat(beerInventoryEvent.getOrderId()).isEqualTo(savedOrder.getId());
    }

    @Test
    void testAllocatedToCancel() throws JsonProcessingException {
        // given
        final var orderId = UUID.randomUUID();
        final var customerId = UUID.randomUUID();
        final var beerCustomerResponse = getCustomer(customerId);
        final var beerDto = getBeerDto(UUID.randomUUID());
        final var beerInventoryResponse = getBeerInventoryResponse(beerDto);
        final var order = createOrder(orderId, customerId, beerDto, 1);
        wireMockServer.stubFor(get(BEER_CUSTOMER_ID + customerId)
                .willReturn(okJson(objectMapper.writeValueAsString(beerCustomerResponse))));
        wireMockServer.stubFor(get(BEER_INVENTORY_ID + beerDto.getId())
                .willReturn(okJson(objectMapper.writeValueAsString(beerInventoryResponse))));

        final var savedOrder = orderManager.newOrder(customerId, order.getItems());
        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(BeerOrderStatus.ALLOCATED, foundOrder.getOrderStatus());
        });

        // when
        orderManager.cancel(savedOrder.getId());

        // then
        await().untilAsserted(() -> {
            final var foundOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(BeerOrderStatus.CANCELLED_BY_SYSTEM, foundOrder.getOrderStatus());
        });
        final var beerInventoryEvent = (BeerInventoryEvent) jmsTemplate.receiveAndConvert(deallocateStockOrderQueueName);
        assertNotNull(beerInventoryEvent);
        assertThat(beerInventoryEvent.getOrderId()).isEqualTo(savedOrder.getId());
    }

    private Order createOrder(UUID orderId, UUID customerId, BeerDto beerDto, int orderedQuantity) {
        final var items = new ArrayList<OrderItem>();
        items.add(new OrderItem(beerDto.getId(), orderedQuantity));

        return Order.builder()
                .id(orderId)
                .customerId(customerId)
                .items(items)
                .build();
    }

    private BeerDto getBeerDto(UUID beerId) {
        return BeerDto.builder()
                .id(beerId)
                .upc("12345")
                .build();
    }

    private BeerInventoryResponse getBeerInventoryResponse(BeerDto beerDto) {
        return new BeerInventoryResponse(
                beerDto.getId(),
                "Beer",
                1,
                BigDecimal.valueOf(10.00)
        );
    }

    private BeerCustomer getCustomer(UUID customerId) {
        return BeerCustomer.builder()
                .id(customerId)
                .build();
    }
}