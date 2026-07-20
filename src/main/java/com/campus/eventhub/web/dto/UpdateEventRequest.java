package com.campus.eventhub.web.dto;

import com.campus.eventhub.domain.EventStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record UpdateEventRequest(
        @NotBlank(message = "Tiêu đề không được để trống") String title,
        String description,
        @NotBlank(message = "Địa điểm không được để trống") String location,
        @NotNull(message = "Thời gian bắt đầu không được để trống") LocalDateTime startAt,
        @NotNull(message = "Số lượng chỗ không được để trống") @Min(value = 1, message = "Sức chứa phải lớn hơn 0") Integer capacity,
        @NotNull(message = "Trạng thái không được để trống") EventStatus status
) {}