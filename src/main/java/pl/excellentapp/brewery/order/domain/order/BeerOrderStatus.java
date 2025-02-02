package pl.excellentapp.brewery.order.domain.order;

public enum BeerOrderStatus {

    NEW, VALIDATED, VALIDATION_EXCEPTION, ALLOCATED, ALLOCATION_EXCEPTION,
    READY, PICKED_UP, CANCELLED, CANCELLED_EXCEPTION, DELIVERED, DELIVER_EXCEPTION
}
