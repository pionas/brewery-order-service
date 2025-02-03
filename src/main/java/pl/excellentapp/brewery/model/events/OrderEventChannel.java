package pl.excellentapp.brewery.model.events;

public interface OrderEventChannel {

    void publish(OrderCreatedEvent event);

    void publish(OrderPickedUpEvent event);

    void publish(OrderCancelledEvent event);
}
