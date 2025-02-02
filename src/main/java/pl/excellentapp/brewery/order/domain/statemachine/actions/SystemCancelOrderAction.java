package pl.excellentapp.brewery.order.domain.statemachine.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;
import pl.excellentapp.brewery.order.domain.order.BeerOrderEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;
import pl.excellentapp.brewery.order.domain.order.OrderRepository;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class SystemCancelOrderAction extends AbstractAction {

    private final OrderRepository beerOrderRepository;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> context) {
        final var beerOrderId = (String) context.getMessageHeader(BeerOrderManager.BEER_ORDER_ID_HEADER);

        beerOrderRepository.findById(UUID.fromString(beerOrderId))
                .ifPresentOrElse(beerOrder -> {
                    // TODO
                    log.info("Order {} was cancellation by system", beerOrderId);
//                    refundIfNecessary(beerOrderId);
//                    notifyUser(beerOrderId);

                }, () -> log.error("Beer Order Not Found!"));
    }
}
