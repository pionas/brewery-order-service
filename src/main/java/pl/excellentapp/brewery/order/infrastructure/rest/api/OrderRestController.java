package pl.excellentapp.brewery.order.infrastructure.rest.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.excellentapp.brewery.order.application.OrderService;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderPagedList;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderRequest;
import pl.excellentapp.brewery.order.infrastructure.rest.api.dto.OrderResponse;
import pl.excellentapp.brewery.order.infrastructure.rest.api.mapper.OrderRestMapper;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
class OrderRestController {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    private final OrderService orderService;
    private final BeerOrderManager beerOrderManager;
    private final OrderRestMapper orderRestMapper;

    @GetMapping({"", "/"})
    public ResponseEntity<OrderPagedList> getOrders(@RequestParam(value = "pageNumber", required = false) Optional<Integer> pageNumber,
                                                    @RequestParam(value = "pageSize", required = false) Optional<Integer> pageSize) {
        return new ResponseEntity<>(orderRestMapper.map(orderService.list(pageNumber.orElse(DEFAULT_PAGE_NUMBER), pageSize.orElse(DEFAULT_PAGE_SIZE))), HttpStatus.OK);
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
        final var orderResponse = orderRestMapper.map(beerOrderManager.newOrder(orderRequest.getCustomerId(), orderRestMapper.mapToOrderItemList(orderRequest.getItems())));

        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<HttpStatus> cancelOrder(@PathVariable("orderId") UUID orderId) {
        orderService.cancelOrder(orderId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<HttpStatus> deleteOrder(@PathVariable("orderId") UUID orderId) {
        orderService.delete(orderId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
