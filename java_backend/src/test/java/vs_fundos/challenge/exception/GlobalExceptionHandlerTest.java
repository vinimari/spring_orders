package vs_fundos.challenge.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleOrderNotFoundException_shouldReturnNotFound() {
        String orderNumber = "ORDER-01";
        OrderNotFoundException exception = new OrderNotFoundException(orderNumber);

        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleOrderNotFoundException(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(body.get("error")).isEqualTo("Not Found");
        assertThat(body.get("message")).isEqualTo("Order not found with number: " + orderNumber);
        assertThat(body.get("timestamp")).isNotNull();
    }

    @Test
    void handleOrderAlreadyProcessedException_shouldReturnConflict() {
        String orderNumber = "ORDER-01";
        OrderAlreadyProcessedException exception = new OrderAlreadyProcessedException(orderNumber);

        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleOrderAlreadyProcessedException(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(body.get("error")).isEqualTo("Conflict");
        assertThat(body.get("message")).isEqualTo("Order already processed: " + orderNumber);
        assertThat(body.get("timestamp")).isNotNull();
    }

    @Test
    void handleJsonConvertionException_shouldReturnBadRequest() {
        String originalMessage = "Error converting Object to JSON";
        JsonConvertionException exception = new JsonConvertionException(originalMessage);

        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleJsonConvertionException(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.get("error")).isEqualTo("Bad Request");
        assertThat(body.get("message")).isEqualTo("Malformed JSON request: " + originalMessage);
        assertThat(body.get("timestamp")).isNotNull();
    }

    @Test
    void handleAllUncaughtExceptions_shouldReturnInternalServerError() {
        String errorMessage = "Generic Error";
        Exception exception = new RuntimeException(errorMessage);

        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleAllUncaughtExceptions(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(body.get("error")).isEqualTo("Internal Server Error");
        assertThat(body.get("message")).isEqualTo(errorMessage);
        assertThat(body.get("timestamp")).isNotNull();
    }

}
