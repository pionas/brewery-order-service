package pl.excellentapp.brewery.order.application.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.excellentapp.brewery.model.events.BeerInventoryEventResponse;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;

@RequiredArgsConstructor
@Component
@Slf4j
class BeerInventoryListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = "${queue.inventory.allocate-response}")
    public void orderAllocation(@Payload BeerInventoryEventResponse result) {
        if (result.success()) {
            beerOrderManager.orderAllocationPassed(result.getOrderId(), result.getBeerId(), result.getStock());
        } else {
            beerOrderManager.orderAllocationFailed(result.getOrderId(), result.getBeerId());
        }
    }
}
