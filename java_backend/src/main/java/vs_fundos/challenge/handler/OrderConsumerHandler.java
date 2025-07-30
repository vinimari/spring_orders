package vs_fundos.challenge.handler;

import vs_fundos.challenge.dto.OrderDTO;

public interface OrderConsumerHandler {
    void handleMessage(OrderDTO orderDTO);
}
