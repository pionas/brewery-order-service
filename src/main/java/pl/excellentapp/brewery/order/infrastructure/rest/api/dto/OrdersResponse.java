package pl.excellentapp.brewery.order.infrastructure.rest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersResponse {

    private List<OrderResponse> orders;
}
