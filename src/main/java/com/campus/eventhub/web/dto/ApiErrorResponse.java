package com.campus.eventhub.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        List<FieldErrorDetail> errors
) {
    public record FieldErrorDetail(
            String field,
            Object rejectedValue,
            String message
    ) {}
}