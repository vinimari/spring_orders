package vs_fundos.challenge.exception;

public class JsonConvertionException extends RuntimeException {

    public JsonConvertionException(String message, Throwable cause) {
        super(message, cause);
    }
    public JsonConvertionException(String message) {
        super(message);
    }
}
