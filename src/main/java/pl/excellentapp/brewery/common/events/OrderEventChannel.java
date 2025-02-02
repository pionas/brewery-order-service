package pl.excellentapp.brewery.common.events;

public interface OrderEventChannel {

    void publish(OrderCreatedEvent event);

    void publish(OrderAllocatedEvent event);

    void publish(OrderPickedUpEvent event);

    void publish(OrderCancelledEvent event);
}
