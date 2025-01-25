package pl.excellentapp.brewery.order.infrastructure.rest.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.excellentapp.brewery.order.application.OrderService;
import pl.excellentapp.brewery.order.domain.exception.OrderNotFoundException;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderRequest;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderResponse;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrdersResponse;
import pl.excellentapp.brewery.order.infrastructure.rest.api.mapper.OrderRestMapper;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
class OrderRestController {

    private final OrderService orderService;
    private final OrderRestMapper orderRestMapper;

    @GetMapping({"", "/"})
    public ResponseEntity<OrdersResponse> getOrders() {
        return new ResponseEntity<>(orderRestMapper.map(orderService.findAll()), HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderId") UUID orderId) {
        return orderService.findById(orderId)
                .map(orderRestMapper::map)
                .map(orderResponse -> new ResponseEntity<>(orderResponse, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        final var orderResponse = orderRestMapper.map(orderService.create(orderRequest.getCustomerId(), orderRestMapper.mapToOrderItemList(orderRequest.getItems())));

        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable("orderId") UUID orderId, @Valid @RequestBody OrderRequest orderRequest) {
        final var orderResponse = orderRestMapper.map(orderService.update(orderId, orderRestMapper.mapToOrderItemList(orderRequest.getItems())));

        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<HttpStatus> deleteOrder(@PathVariable("orderId") UUID orderId) {
        orderService.delete(orderId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Object> handle(OrderNotFoundException ex) {
        return handleError(HttpStatus.NOT_FOUND, List.of(ex.getMessage()));
    }

    private ResponseEntity<Object> handleError(HttpStatus status, List<String> errors) {
        final var body = new LinkedHashMap<String, Object>();
        body.put("timestamp", new Date());
        body.put("status", status.value());
        body.put("errors", errors);
        return new ResponseEntity<>(body, status);
    }

}
