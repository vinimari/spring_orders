package vs_fundos.challenge.exception;

public class OrderAlreadyProcessedException extends RuntimeException {
    public OrderAlreadyProcessedException (String orderNumber) {
        super("Order already processed: " + orderNumber);
    }
}
