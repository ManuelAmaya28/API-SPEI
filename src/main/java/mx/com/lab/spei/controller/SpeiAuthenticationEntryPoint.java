package mx.com.lab.spei.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Custom {@link AuthenticationEntryPoint} that converts Spring Security authentication
 * failures into structured JSON responses with a {@code {"message": "..."}} body.
 *
 * <p>Covers four distinct cases (Requirements 1.1, 1.2, 1.5, 1.6):</p>
 * <ul>
 *   <li>Missing {@code Authorization} header → HTTP 401</li>
 *   <li>Non-Bearer {@code Authorization} header → HTTP 400</li>
 *   <li>JWKS endpoint unreachable → HTTP 503</li>
 *   <li>Invalid / expired JWT → HTTP 401</li>
 * </ul>
 *
 * <p>Requirements: 1.1, 1.2, 1.5, 1.6</p>
 */
@Component
public class SpeiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String authHeader = request.getHeader("Authorization");

        int status;
        String message;

        if (authHeader == null || authHeader.isBlank()) {
            status = HttpStatus.UNAUTHORIZED.value();
            message = "Authorization header is required";
        } else if (!authHeader.startsWith("Bearer ")) {
            status = HttpStatus.BAD_REQUEST.value();
            message = "Authorization header must use Bearer scheme";
        } else {
            // Distinguish JWKS connectivity failures from ordinary token errors
            Throwable cause = authException.getCause();
            boolean jwksUnreachable = cause != null &&
                (cause.getClass().getSimpleName().contains("JwkException") ||
                 cause.getClass().getSimpleName().contains("RestClientException") ||
                 cause.getClass().getSimpleName().contains("ConnectException") ||
                 (cause.getMessage() != null && cause.getMessage().contains("JWKS")));
            if (jwksUnreachable) {
                status = HttpStatus.SERVICE_UNAVAILABLE.value();
                message = "Authentication service is temporarily unavailable";
            } else {
                status = HttpStatus.UNAUTHORIZED.value();
                message = "Bearer token is invalid or expired";
            }
        }

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}
