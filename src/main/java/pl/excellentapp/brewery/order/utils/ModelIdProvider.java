package pl.excellentapp.brewery.order.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ModelIdProvider {

    public UUID random() {
        return UUID.randomUUID();
    }
}
