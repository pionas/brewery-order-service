package pl.excellentapp.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeerInventoryEventResponse implements Serializable {

    private UUID orderId;
    private Map<UUID, Integer> beers = new HashMap<>();
    private Boolean success;
    private String message;

    public boolean success() {
        return Boolean.TRUE.equals(success);
    }
}
