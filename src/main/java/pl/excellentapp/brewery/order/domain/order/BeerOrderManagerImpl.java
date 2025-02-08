package pl.excellentapp.brewery.order.domain.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.stereotype.Service;
import pl.excellentapp.brewery.order.application.OrderFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    private static final int MAX_LOOP_COUNT = 10;
    private static final int DELAY_ELEMENT_IN_MILLIS = 200;

    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> stateMachineFactory;
    private final OrderRepository beerOrderRepository;
    private final OrderFactory orderFactory;
    private final StateMachineInterceptorAdapter<BeerOrderStatus, BeerOrderEvent> beerOrderStateChangeInterceptor;

    @Override
    public Order newOrder(UUID customerId, List<OrderItem> orderItems) {
        Order order = orderFactory.createOrder(customerId, orderItems);
        Order savedOrder = beerOrderRepository.save(order);
        sendOrderEvent(savedOrder, BeerOrderEvent.VALIDATE_ORDER)
                .subscribe();
        return savedOrder;
    }

    @Override
    public void processValidationResult(UUID orderId, Boolean isValid) {
        log.debug("Process Validation Result for orderId: {} Valid? {}", orderId, isValid);
        sleep(orderId, List.of(BeerOrderStatus.VALIDATION_PENDING));
        beerOrderRepository.findById(orderId)
                .ifPresentOrElse(beerOrder -> {
                    final var event = isValid ? BeerOrderEvent.VALIDATION_PASSED : BeerOrderEvent.VALIDATION_FAILED;
                    sendOrderEvent(beerOrder, event)
                            .subscribe();
                    if (isValid) {
                        sleep(orderId, List.of(BeerOrderStatus.VALIDATED));
                        beerOrderRepository.findById(orderId).ifPresent(order -> sendOrderEvent(order, BeerOrderEvent.ALLOCATE_ORDER)
                                .subscribe());
                    }
                }, () -> logOrderNotFound(orderId));
    }

    @Override
    public void orderAllocationPassed(UUID orderId, Map<UUID, Integer> beers) {
        sleep(orderId, List.of(BeerOrderStatus.ALLOCATED));
        beerOrderRepository.findById(orderId)
                .ifPresentOrElse(beerOrder -> {
                            beers.forEach(beerOrder::reserve);
                            beerOrderRepository.save(beerOrder);
                            final var event = beerOrder.isReady() ? BeerOrderEvent.ALLOCATION_SUCCESS : BeerOrderEvent.ALLOCATION_NO_INVENTORY;
                            sendOrderEvent(beerOrder, event)
                                    .subscribe();
                        },
                        () -> logOrderNotFound(orderId)
                );
    }

    @Override
    public void orderAllocationFailed(UUID orderId, Map<UUID, Integer> beers) {
        sleep(orderId, List.of(BeerOrderStatus.ALLOCATED));
        beerOrderRepository.findById(orderId).ifPresentOrElse(
                beerOrder -> sendOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_FAILED)
                        .subscribe(),
                () -> logOrderNotFound(orderId)
        );
    }

    @Override
    public void orderPickedUp(UUID orderId) {
        sleep(orderId, List.of(BeerOrderStatus.ALLOCATED));
        beerOrderRepository.findById(orderId).ifPresentOrElse(
                beerOrder -> sendOrderEvent(beerOrder, BeerOrderEvent.BEER_ORDER_PICKED_UP)
                        .subscribe(),
                () -> logOrderNotFound(orderId)
        );
    }

    @Override
    public void cancel(UUID orderId) {
        sleep(orderId, List.of(BeerOrderStatus.VALIDATION_PENDING, BeerOrderStatus.VALIDATED, BeerOrderStatus.ALLOCATION_PENDING, BeerOrderStatus.ALLOCATED));
        beerOrderRepository.findById(orderId).ifPresentOrElse(
                beerOrder -> sendOrderEvent(beerOrder, BeerOrderEvent.CANCEL_BY_SYSTEM)
                        .subscribe(),
                () -> logOrderNotFound(orderId)
        );
    }

    private Mono<Void> sendOrderEvent(Order beerOrder, BeerOrderEvent event) {
        return build(beerOrder)
                .doOnSubscribe(subscription -> log.debug("Subscribing to send event: {}", event))
                .flatMap(sm -> {
                    final var message = MessageBuilder.withPayload(event)
                            .setHeader(BEER_ORDER_ID_HEADER, beerOrder.getId().toString())
                            .build();
                    return sm.sendEvent(Mono.just(message))
                            .doOnNext(result -> log.debug("Event sent successfully: {}", event))
                            .doOnError(e -> log.error("Error sending event: {}", event, e))
                            .then();
                })
                .doOnSuccess(aVoid -> log.debug("Completed sendOrderEvent for event: {}", event))
                .doOnError(e -> log.error("Failed to send order event", e));
    }

    private synchronized Mono<StateMachine<BeerOrderStatus, BeerOrderEvent>> build(Order beerOrder) {
        final var stateMachine = stateMachineFactory.getStateMachine(beerOrder.getId());
        return stateMachine.stopReactively()
                .then(Mono.defer(() -> {
                    stateMachine.getStateMachineAccessor()
                            .doWithAllRegions(paymentStatePaymentEventStateMachineAccess -> {
                                paymentStatePaymentEventStateMachineAccess.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                                paymentStatePaymentEventStateMachineAccess.resetStateMachineReactively(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null))
                                        .subscribe();
                            });
                    return Mono.empty();
                }))
                .then(stateMachine.startReactively())
                .thenReturn(stateMachine);
    }

    private void logOrderNotFound(UUID beerOrderId) {
        log.error("Order Not Found. Id: {}", beerOrderId);
    }

    private void sleep(UUID orderId, List<BeerOrderStatus> expectedOrderStatuses) {
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);
        while (!found.get()) {
            if (loopCount.incrementAndGet() > MAX_LOOP_COUNT) {
                found.set(true);
                log.debug("Loop Retries exceeded");
            }
            beerOrderRepository.findById(orderId)
                    .ifPresentOrElse(beerOrder -> {
                        if (expectedOrderStatuses.contains(beerOrder.getOrderStatus())) {
                            found.set(true);
                            log.debug("Order Found");
                        } else {
                            log.debug("Order Status Not Equal. Expected: {} Found: {}", expectedOrderStatuses, beerOrder.getOrderStatus().name());
                        }
                    }, () -> log.debug("Order Id Not Found"));
            if (!found.get()) {
                try {
                    Thread.sleep(DELAY_ELEMENT_IN_MILLIS);
                } catch (InterruptedException e) {

                }
            }
        }
    }
}
