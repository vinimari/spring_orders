package vs_fundos.challenge.producer;

import vs_fundos.challenge.dto.OrderDTO;

public interface OrderProducerService {
    void sendMessage(OrderDTO orderDTO);
}
