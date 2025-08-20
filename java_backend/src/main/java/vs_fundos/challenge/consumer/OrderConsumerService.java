package vs_fundos.challenge.consumer;

import vs_fundos.challenge.dto.OrderDTO;

public interface OrderConsumerService {
    void listen(OrderDTO orderDTO);
}
