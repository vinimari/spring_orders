package vs_fundos.challenge.service;

import vs_fundos.challenge.dto.OrderDTO;

public interface OrderService {
    OrderDTO createRandomOrder();
    OrderDTO createOrder(OrderDTO orderDTO);
    OrderDTO updateById(Long id, OrderDTO orderDetails);
    OrderDTO getOrderById(Long id);
}
