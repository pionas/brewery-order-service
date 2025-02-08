package pl.excellentapp.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeerValidationResponse implements Serializable {

    private UUID orderId;
    private Boolean success;

    public boolean success() {
        return Boolean.TRUE.equals(success);
    }
}
