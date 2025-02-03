package pl.excellentapp.brewery.order.infrastructure.rest.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.excellentapp.brewery.order.application.OrderService;
import pl.excellentapp.brewery.order.domain.exception.OrderNotFoundException;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;
import pl.excellentapp.brewery.order.domain.order.Order;
import pl.excellentapp.brewery.order.domain.order.OrderPage;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderItemRequest;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderPagedList;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderRequest;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderResponse;
import pl.excellentapp.brewery.order.infrastructure.rest.api.mapper.OrderRestMapper;
import pl.excellentapp.brewery.order.infrastructure.rest.handler.GlobalExceptionHandler;
import pl.excellentapp.brewery.order.utils.DateTimeProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderRestControllerTest extends AbstractMvcTest {

    @InjectMocks
    private OrderRestController controller;

    @Mock
    private OrderService orderService;

    @Mock
    private BeerOrderManager orderManager;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @Spy
    private OrderRestMapper orderRestMapper = Mappers.getMapper(OrderRestMapper.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(dateTimeProvider))
                .build();
    }

    @Test
    void shouldReturnEmptyListOfOrders() throws Exception {
        // given

        // when
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk());

        // then
        verify(orderService).list(any(), any());
    }

    @Test
    void shouldReturnListOfOrders() throws Exception {
        // given
        final var order1 = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"));
        final var order2 = createOrder(UUID.fromString("4a5b96de-684a-411b-9616-fddd0b06a382"));
        final var orderPage = OrderPage.of(Pair.of(List.of(order1, order2), 2), 1, 2);
        when(orderService.list(any(), any())).thenReturn(orderPage);

        // when
        final var response = mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        // then
        assertNotNull(response);
        final var responseBody = response.getContentAsString();
        assertNotNull(responseBody);
        final var ordersResponse = super.mapFromJson(responseBody, OrderPagedList.class);
        assertNotNull(ordersResponse);
        final var ordersResponseList = ordersResponse.getContent();
        assertNotNull(ordersResponseList);
        assertEquals(2, ordersResponseList.size());
        final var orderResponse1 = ordersResponseList.getFirst();
        assertNotNull(orderResponse1);
        assertEquals(orderResponse1.getId(), order1.getId());
        final var orderResponse2 = ordersResponseList.getLast();
        assertNotNull(orderResponse2);
        assertEquals(orderResponse2.getId(), order2.getId());
        verify(orderService).list(any(), any());
    }

    @Test
    void shouldReturnNotFoundWhenOrderByIdNotExists() throws Exception {
        // given
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        // when
        final var response = mockMvc.perform(get("/api/v1/orders/" + orderId))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();

        // then
        assertNotNull(response);
        final var responseBody = response.getContentAsString();
        assertNotNull(responseBody);
        verify(orderService).findById(orderId);
    }

    @Test
    void shouldReturnOrderById() throws Exception {
        // given
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        final var order = createOrder(orderId);
        when(orderService.findById(orderId)).thenReturn(Optional.of(order));

        // when
        final var response = mockMvc.perform(get("/api/v1/orders/" + orderId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        // then
        assertNotNull(response);
        final var responseBody = response.getContentAsString();
        assertNotNull(responseBody);
        final var orderResponse = super.mapFromJson(responseBody, OrderResponse.class);
        assertNotNull(orderResponse);
        assertEquals(orderResponse.getId(), order.getId());
    }

    @Test
    void shouldCreateOrder() throws Exception {
        // given
        final var order = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"));
        final var customerId = UUID.fromString("ec85f68b-eb10-4b1a-b1ee-091719ebc4b4");
        final var orderItem = new OrderItemRequest(UUID.randomUUID(), 1);
        final var orderRequest = OrderRequest.builder()
                .customerId(customerId)
                .items(List.of(orderItem))
                .build();
        when(orderManager.newOrder(any(), any())).thenReturn(order);

        // when
        final var response = mockMvc.perform(post("/api/v1/orders")
                        .content(super.mapToJson(orderRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        // then
        assertNotNull(response);
        final var responseBody = response.getContentAsString();
        assertNotNull(responseBody);
        final var orderResponse = super.mapFromJson(responseBody, OrderResponse.class);
        assertNotNull(orderResponse);
        assertEquals(orderResponse.getId(), order.getId());
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        // given
        final var customerId = UUID.fromString("ec85f68b-eb10-4b1a-b1ee-091719ebc4b4");
        final var originalOrder = createOrder(UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936"));
        final var orderItem = new OrderItemRequest(UUID.randomUUID(), 1);
        final var orderRequest = OrderRequest.builder()
                .customerId(customerId)
                .items(List.of(orderItem))
                .build();
        when(orderService.update(any(), any())).thenReturn(originalOrder);

        // when
        final var response = mockMvc.perform(put("/api/v1/orders/" + originalOrder.getId())
                        .content(super.mapToJson(orderRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        // then
        assertNotNull(response);
        final var responseBody = response.getContentAsString();
        assertNotNull(responseBody);
        final var orderResponse = super.mapFromJson(responseBody, OrderResponse.class);
        assertNotNull(orderResponse);
        assertEquals(orderResponse.getId(), originalOrder.getId());
        verify(orderService).update(any(), any());
    }

    @Test
    void shouldThrowNotFoundWhenTryUpdateOrderByOrderByIdNotExists() throws Exception {
        // given
        final var customerId = UUID.fromString("ec85f68b-eb10-4b1a-b1ee-091719ebc4b4");
        final var originalOrder = createOrder(UUID.fromString("be09a70b-8916-41de-9299-998decf259d5"));
        final var orderItem = new OrderItemRequest(UUID.randomUUID(), 1);
        final var orderRequest = OrderRequest.builder()
                .customerId(customerId)
                .items(List.of(orderItem))
                .build();
        when(orderService.update(any(), any())).thenThrow(new OrderNotFoundException("Error occurred"));

        // when
        mockMvc.perform(put("/api/v1/orders/" + originalOrder.getId())
                        .content(super.mapToJson(orderRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // then
        verify(orderService).update(any(), any());
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        // given
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");

        // when
        mockMvc.perform(delete("/api/v1/orders/" + orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        // then
        verify(orderService).delete(orderId);
    }

    @Test
    void shouldThrowExceptionWhenTryDeleteOrderButOrderByIdNotExists() throws Exception {
        // given
        final var orderId = UUID.fromString("71737f0e-11eb-4775-b8b4-ce945fdee936");
        doThrow(new OrderNotFoundException("Order Not Found. UUID: " + orderId)).when(orderService).delete(orderId);

        // when
        mockMvc.perform(delete("/api/v1/orders/" + orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // then
        verify(orderService).delete(orderId);
    }

    private Order createOrder(UUID id) {
        return Order.builder()
                .id(id)
                .build();
    }
}