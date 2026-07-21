package com.campus.eventhub.security;

import com.campus.eventhub.web.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RestSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestSecurityExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 401 Unauthorized
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        writeErrorResponse(request, response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Yêu cầu xác thực tài khoản hợp lệ.");
    }

    // 403 Forbidden
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        writeErrorResponse(request, response, HttpStatus.FORBIDDEN, "FORBIDDEN", "Bạn không có quyền truy cập tài nguyên này.");
    }

    private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                    HttpStatus status, String errorCode, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorCode,
                message,
                request.getRequestURI(),
                null
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}