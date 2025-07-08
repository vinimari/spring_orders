package vs_fundos.challenge.interceptor;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrderInterceptorTest {
    private static final String TOKEN_HEADER_NAME = "token";
    private static final String VALID_TOKEN_VALUE = "token_value";
    private static final String INVALID_TOKEN_VALUE = "invalid_token";
    private MockHttpServletResponse response;
    private MockHttpServletRequest request;
    private Object handler;

    @InjectMocks
    private OrderInterceptor orderInterceptor;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        handler = new Object();
        orderInterceptor = new OrderInterceptor();
    }

    @Test
    void preHandle_shouldReturnTrue_whenTokenHeaderIsValid() throws Exception {
        request.addHeader(TOKEN_HEADER_NAME, VALID_TOKEN_VALUE);

        boolean result = orderInterceptor.preHandle(request, response, handler);

        assertTrue(result);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void preHandle_shoudReturnFalse_whenTokenHeaderIsMissing() throws Exception {
        String expectedErrorMessage = "Header '" + TOKEN_HEADER_NAME + "' missing or invalid.";

        boolean result = orderInterceptor.preHandle(request, response, handler);

        assertFalse(result);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals(expectedErrorMessage, response.getContentAsString());
    }

    @Test
    void preHandle_shouldReturnFalse_whenTokenHeaderIsInvalid() throws Exception {
        request.addHeader(TOKEN_HEADER_NAME, INVALID_TOKEN_VALUE);
        String expectedErrorMessage = "Header '" + TOKEN_HEADER_NAME + "' missing or invalid.";

        boolean result = orderInterceptor.preHandle(request, response, handler);

        assertFalse(result);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals(expectedErrorMessage, response.getContentAsString());
    }
}
