package vs_fundos.challenge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.exception.ResponseEncryptionException;
import vs_fundos.challenge.util.Cryptography;

@ControllerAdvice
@RequiredArgsConstructor
public class OrderResponseAdvice implements ResponseBodyAdvice<Object> {

    private final Cryptography cryptography;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> bodyType = getBodyType(returnType);
        return bodyType.equals(OrderDTO.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof OrderDTO orderDTO) {
            String originalOrderNumber = orderDTO.getOrderNumber();
            String encryptedOrderNumber = null;
            try {
                encryptedOrderNumber = cryptography.encrypt(originalOrderNumber);
            } catch (Exception e) {
                String errorMessage = "Failed to encrypt the order number: " + originalOrderNumber;
                throw new ResponseEncryptionException(errorMessage, e);
            }
            orderDTO.setOrderNumber(encryptedOrderNumber);
            return orderDTO;
        }
        return body;
    }

    private Class<?> getBodyType(MethodParameter returnType) {
        if (ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return ResolvableType.forMethodParameter(returnType).getGeneric(0).resolve();
        }
        return returnType.getParameterType();
    }
}
