package com.campus.eventhub.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record RegisterEventRequest(
        @NotNull(message = "Thông tin người đăng ký là bắt buộc")
        @Valid // Kích hoạt Cascading Validation
        ParticipantDto participant
) {
    public record ParticipantDto(
            @NotBlank(message = "Tên người tham gia không được để trống")
            @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
            String name,

            @NotBlank(message = "Email không được để trống")
            @Email(message = "Email không đúng định dạng")
            String email
    ) {}
}