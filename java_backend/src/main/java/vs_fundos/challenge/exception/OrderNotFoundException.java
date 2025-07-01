package vs_fundos.challenge.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderNumber) {
        super("Order not found with number: " + orderNumber);
    }
}
