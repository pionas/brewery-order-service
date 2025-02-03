package pl.excellentapp.brewery.model.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@Setter
@Getter
public class BeerInventoryEventResponse implements Serializable {

    private UUID orderId;
    private UUID beerId;
    private Integer stock;
    private Boolean success;
    private String message;

    public boolean success() {
        return Boolean.TRUE.equals(success);
    }
}
