package vs_fundos.challenge.event;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vs_fundos.challenge.dto.OrderDTO;

@Getter
@RequiredArgsConstructor
public class OrderCreatedEvent {
    private final OrderDTO orderDTO;
}
