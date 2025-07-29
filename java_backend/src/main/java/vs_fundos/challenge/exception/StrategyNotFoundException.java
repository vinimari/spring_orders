package vs_fundos.challenge.exception;

public class StrategyNotFoundException extends IllegalArgumentException  {
    public StrategyNotFoundException(String message) {
        super("No notification strategy found for type: " + message);
    }
}
