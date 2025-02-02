package pl.excellentapp.brewery.order.domain.statemachine.guards;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;
import pl.excellentapp.brewery.order.domain.order.BeerOrderEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderManager;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;

@Component
class BeerOrderIdGuard implements Guard<BeerOrderStatus, BeerOrderEvent> {

    @Override
    public boolean evaluate(StateContext<BeerOrderStatus, BeerOrderEvent> context) {
        return context.getMessageHeader(BeerOrderManager.BEER_ORDER_ID_HEADER) != null;
    }
}