package pl.excellentapp.brewery.order.domain.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.excellentapp.brewery.order.application.OrderFactory;
import pl.excellentapp.brewery.order.application.OrderUpdated;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> stateMachineFactory;
    private final OrderRepository beerOrderRepository;
    private final OrderFactory orderFactory;
    private final OrderUpdated orderUpdated;
    private final StateMachineInterceptorAdapter<BeerOrderStatus, BeerOrderEvent> beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public Order newOrder(UUID customerId, List<OrderItem> orderItems) {
        Order order = orderFactory.createOrder(customerId, orderItems);
        Order savedOrder = beerOrderRepository.save(order);
        sendOrderEvent(savedOrder, BeerOrderEvent.VALIDATE_ORDER);
        return savedOrder;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {
        log.debug("Process Validation Result for beerOrderId: {} Valid? {}", beerOrderId, isValid);
        beerOrderRepository.findById(beerOrderId)
                .ifPresentOrElse(beerOrder -> {
                    if (isValid) {
                        sendOrderEvent(beerOrder, BeerOrderEvent.VALIDATION_PASSED);

                        awaitForStatus(beerOrderId, BeerOrderStatus.VALIDATED);
                        beerOrderRepository.findById(beerOrderId)
                                .ifPresent(validatedOrder -> sendOrderEvent(validatedOrder, BeerOrderEvent.ALLOCATE_ORDER));
                    } else {
                        sendOrderEvent(beerOrder, BeerOrderEvent.VALIDATION_FAILED);
                    }
                }, () -> logOrderNotFound(beerOrderId));
    }

    @Override
    public void orderAllocationPassed(Order beerOrderDto) {
        beerOrderRepository.findById(beerOrderDto.getId())
                .ifPresentOrElse(
                        beerOrder -> {
                            sendOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_SUCCESS);
                            awaitForStatus(beerOrder.getId(), BeerOrderStatus.ALLOCATED);
                            updateAllocatedQty(beerOrderDto);
                        },
                        () -> logOrderNotFound(beerOrderDto.getId())
                );
    }

    @Override
    public void orderAllocationPendingInventory(Order beerOrderDto) {
        beerOrderRepository.findById(beerOrderDto.getId())
                .ifPresentOrElse(
                        beerOrder -> {
                            sendOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_NO_INVENTORY);
                            awaitForStatus(beerOrder.getId(), BeerOrderStatus.PENDING_INVENTORY);
                            updateAllocatedQty(beerOrderDto);
                        },
                        () -> logOrderNotFound(beerOrderDto.getId())
                );
    }

    @Override
    public void orderAllocationFailed(Order beerOrderDto) {
        beerOrderRepository.findById(beerOrderDto.getId()).ifPresentOrElse(
                beerOrder -> sendOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_FAILED),
                () -> logOrderNotFound(beerOrderDto.getId())
        );
    }

    @Override
    public void orderPickedUp(UUID id) {
        beerOrderRepository.findById(id).ifPresentOrElse(
                beerOrder -> sendOrderEvent(beerOrder, BeerOrderEvent.BEER_ORDER_PICKED_UP),
                () -> logOrderNotFound(id)
        );
    }

    @Override
    public void cancel(UUID id) {
        beerOrderRepository.findById(id).ifPresentOrElse(
                beerOrder -> sendOrderEvent(beerOrder, BeerOrderEvent.CANCEL_ORDER),
                () -> logOrderNotFound(id)
        );
    }

    private void sendOrderEvent(Order beerOrder, BeerOrderEvent event) {
        final var sm = build(beerOrder);
        final var paymentEventMessage = MessageBuilder.withPayload(event)
                .setHeader(BEER_ORDER_ID_HEADER, beerOrder.getId().toString())
                .build();

        sm.sendEvent(Mono.just(paymentEventMessage)).blockLast();
    }

    private void awaitForStatus(UUID beerOrderId, BeerOrderStatus orderStatus) {
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);
        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(true);
                log.debug("Loop Retries exceeded");
            }
            beerOrderRepository.findById(beerOrderId)
                    .ifPresentOrElse(beerOrder -> {
                        if (beerOrder.getOrderStatus().equals(orderStatus)) {
                            found.set(true);
                            log.debug("Order Found");
                        } else {
                            log.debug("Order Status Not Equal. Expected: {} Found: {}", orderStatus.name(), beerOrder.getOrderStatus().name());
                        }
                    }, () -> log.debug("Order Id Not Found"));

            if (!found.get()) {
                try {
                    log.debug("Sleeping for retry");
                    Thread.sleep(100);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
    }

    private StateMachine<BeerOrderStatus, BeerOrderEvent> build(Order beerOrder) {
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
                .thenReturn(stateMachine)
                .block();
    }

    private void updateAllocatedQty(Order beerOrderDto) {
        beerOrderRepository.findById(beerOrderDto.getId())
                .ifPresentOrElse(
                        beerOrder -> {
                            // TODO: update quantity allocated
//                            beerOrder.
                            beerOrderRepository.save(beerOrder);
                        },
                        () -> logOrderNotFound(beerOrderDto.getId())
                );
    }

    private void logOrderNotFound(UUID beerOrderId) {
        log.error("Order Not Found. Id: " + beerOrderId);
    }
}
