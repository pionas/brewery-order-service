package pl.excellentapp.brewery.order.infrastructure.rest.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotNull(message = "Customer ID cannot be null")
    private UUID customerId;

    @NotNull(message = "Order items cannot be null")
    @Size(min = 1, message = "At least one order item is required")
    private List<OrderItemRequest> items;

    @Positive(message = "Total price must be positive")
    private BigDecimal totalPrice;

}
