package pl.excellentapp.brewery.order.domain.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import pl.excellentapp.brewery.model.BeerDto;
import pl.excellentapp.brewery.model.events.BeerInventoryEvent;
import pl.excellentapp.brewery.order.infrastructure.rest.api.AbstractIT;
import pl.excellentapp.brewery.order.infrastructure.service.beerinventory.BeerInventoryResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BeerOrderManagerImplIT extends AbstractIT {

    private static final String BEER_INVENTORY_ID = "/api/v1/inventories/";
    private static final String BEER_UPC = "/api/v1/beers/upc/{beerId}";

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

    @Value("${queue.order.deallocate}")
    private String deallocateOrderQueueName;

    @Value("${queue.order.allocate-failure}")
    private String allocateFailureOrderQueueName;

    private UUID beerId = UUID.randomUUID();
    private UUID customerId = UUID.randomUUID();

    @TestConfiguration
    static class RestTemplateBuilderProvider {

        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8888));
            wireMockServer.start();
            return wireMockServer;
        }
    }

    @Test
    void testNewToAllocated() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

        wireMockServer.stubFor(get(BEER_UPC + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        Order order = createOrder();

        Order savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();

            assertEquals(BeerOrderStatus.ALLOCATED, foundOrder.getOrderStatus());
        });

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();
            OrderItem orderItem = foundOrder.getItems().getFirst();
            assertEquals(orderItem.getOrderedQuantity(), orderItem.getReservedQuantity());
        });

        Order savedOrder2 = orderRepository.findById(savedOrder.getId()).get();

        assertNotNull(savedOrder2);
        assertEquals(BeerOrderStatus.ALLOCATED, savedOrder2.getOrderStatus());
        savedOrder2.getItems().forEach(line -> {
            assertEquals(line.getOrderedQuantity(), line.getReservedQuantity());
        });
    }

    @Test
    void testFailedValidation() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        BeerInventoryResponse beerInventoryResponse = new BeerInventoryResponse(
                beerDto.getId(),
                "Beer",
                1,
                BigDecimal.valueOf(10.00)
        );

        wireMockServer.stubFor(get(BEER_INVENTORY_ID + beerDto.getId())
                .willReturn(okJson(objectMapper.writeValueAsString(beerInventoryResponse))));

        wireMockServer.stubFor(get(BEER_UPC + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        Order order = createOrder();
        order.setCustomerId(UUID.randomUUID());
        Order savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();

            assertEquals(BeerOrderStatus.VALIDATION_EXCEPTION, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

        wireMockServer.stubFor(get(BEER_UPC + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        Order order = createOrder();

        Order savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertEquals(BeerOrderStatus.ALLOCATED, foundOrder.getOrderStatus());
        });

        orderManager.orderPickedUp(savedOrder.getId());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertEquals(BeerOrderStatus.PICKED_UP, foundOrder.getOrderStatus());
        });

        Order pickedUpOrder = orderRepository.findById(savedOrder.getId()).get();

        assertEquals(BeerOrderStatus.PICKED_UP, pickedUpOrder.getOrderStatus());
    }

    @Test
    void testAllocationFailure() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

        wireMockServer.stubFor(get(BEER_UPC + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        Order order = createOrder();
        order.setCustomerId(UUID.randomUUID());

        Order savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertEquals(BeerOrderStatus.ALLOCATION_EXCEPTION, foundOrder.getOrderStatus());
        });

        BeerInventoryEvent allocationFailureEvent = (BeerInventoryEvent) jmsTemplate.receiveAndConvert(allocateFailureOrderQueueName);

        assertNotNull(allocationFailureEvent);
        assertThat(allocationFailureEvent.getOrderId()).isEqualTo(savedOrder.getId());
    }

    @Test
    void testPartialAllocation() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

        wireMockServer.stubFor(get(BEER_UPC + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        Order order = createOrder();
        order.setCustomerId(UUID.randomUUID());

        Order savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertEquals(BeerOrderStatus.PENDING_INVENTORY, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testValidationPendingToCancel() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

        wireMockServer.stubFor(get(BEER_UPC + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        Order order = createOrder();
        order.setCustomerId(UUID.randomUUID());

        Order savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertEquals(BeerOrderStatus.VALIDATION_PENDING, foundOrder.getOrderStatus());
        });

        orderManager.cancel(savedOrder.getId());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertEquals(BeerOrderStatus.CANCELLED_BY_SYSTEM, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testAllocationPendingToCancel() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

        wireMockServer.stubFor(get(BEER_UPC + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        Order order = createOrder();
        order.setCustomerId(UUID.randomUUID());

        Order savedOrder = orderManager.newOrder(order.getCustomerId(), order.getItems());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertEquals(BeerOrderStatus.ALLOCATION_PENDING, foundOrder.getOrderStatus());
        });

        orderManager.cancel(savedOrder.getId());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertEquals(BeerOrderStatus.CANCELLED_BY_USER, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testAllocatedToCancel() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

        wireMockServer.stubFor(get(BEER_UPC + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        Order order = createOrder();

        Order savedOrder = orderManager.newOrder(customerId, order.getItems());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertEquals(BeerOrderStatus.ALLOCATED, foundOrder.getOrderStatus());
        });

        orderManager.cancel(savedOrder.getId());

        await().untilAsserted(() -> {
            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertEquals(BeerOrderStatus.CANCELLED_BY_SYSTEM, foundOrder.getOrderStatus());
        });

        BeerInventoryEvent beerInventoryEvent = (BeerInventoryEvent) jmsTemplate.receiveAndConvert(deallocateOrderQueueName);

        assertNotNull(beerInventoryEvent);
        assertThat(beerInventoryEvent.getOrderId()).isEqualTo(savedOrder.getId());
    }

    public Order createOrder() {
        final var items = new ArrayList<OrderItem>();
        items.add(new OrderItem(beerId, 1));

        return Order.builder()
                .customerId(customerId)
                .items(items)
                .build();
    }
}