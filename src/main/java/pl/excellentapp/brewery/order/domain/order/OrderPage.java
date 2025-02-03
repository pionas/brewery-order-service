package pl.excellentapp.brewery.order.domain.order;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.util.Pair;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class OrderPage {

    private List<Order> orders;
    private int pageNumber;
    private int pageSize;
    private int total;

    public static OrderPage of(Pair<List<Order>, Integer> list, int pageNumber, int pageSize) {
        return OrderPage.builder()
                .orders(list.getFirst())
                .total(list.getSecond())
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();
    }
}