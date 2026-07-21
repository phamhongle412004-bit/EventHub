package com.campus.eventhub.web.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank(message = "Tiêu đề không được để trống")
        @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
        String title,

        @NotBlank(message = "Mô tả không được để trống")
        @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
        String description,

        @NotBlank(message = "Địa điểm không được để trống")
        @Size(max = 255, message = "Địa điểm không được vượt quá 255 ký tự")
        String location,

        @NotNull(message = "Thời gian bắt đầu là bắt buộc")
        @Future(message = "Thời gian bắt đầu phải nằm ở tương lai")
        LocalDateTime startAt,

        @NotNull(message = "Sức chứa là bắt buộc")
        @Positive(message = "Sức chứa phải là số nguyên dương")
        Integer capacity

) {}