package vs_fundos.challenge.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vs_fundos.challenge.interceptor.OrderInterceptor;

@Configuration
@RequiredArgsConstructor
public class AppConfig implements WebMvcConfigurer  {
    private final OrderInterceptor orderInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderInterceptor).addPathPatterns("/order/random");
    }
}
