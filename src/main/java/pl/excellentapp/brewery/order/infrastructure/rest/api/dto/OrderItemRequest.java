package pl.excellentapp.brewery.order.infrastructure.rest.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    @NotNull(message = "Beer ID cannot be null")
    private UUID beerId;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
