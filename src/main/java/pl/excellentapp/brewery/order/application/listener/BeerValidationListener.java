package pl.excellentapp.brewery.order.application.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.excellentapp.brewery.model.events.BeerValidationResponse;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;

@RequiredArgsConstructor
@Component
@Slf4j
class BeerValidationListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = "${queue.beer.validate-response}")
    public void beerValidation(@Payload BeerValidationResponse result) {
        beerOrderManager.processValidationResult(result.getOrderId(), result.success());
    }
}