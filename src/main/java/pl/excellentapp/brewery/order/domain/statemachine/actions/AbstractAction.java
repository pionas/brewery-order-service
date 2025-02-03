package pl.excellentapp.brewery.order.domain.statemachine.actions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.action.Action;
import pl.excellentapp.brewery.order.domain.order.BeerOrderEvent;
import pl.excellentapp.brewery.order.domain.order.BeerOrderStatus;

@Slf4j
public abstract class AbstractAction implements Action<BeerOrderStatus, BeerOrderEvent> {

}
