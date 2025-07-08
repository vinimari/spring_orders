package vs_fundos.challenge.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class OrderInterceptor implements HandlerInterceptor {
    private static final String TOKEN = "token";
    private static final String TOKEN_VALUE = "token_value";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tokenValue = request.getHeader(TOKEN);
        if (TOKEN_VALUE.equals(tokenValue)) {
            return true;
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Header '" + TOKEN + "' missing or invalid.");
            return false;
        }
    }
}
