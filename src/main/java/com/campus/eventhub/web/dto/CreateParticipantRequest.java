package com.campus.eventhub.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateParticipantRequest(
        @NotBlank(message = "Tên người tham gia không được để trống")
        @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
        String fullName,

        @NotBlank(message = "Email là bắt buộc")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 150, message = "Email không được vượt quá 150 ký tự")
        String email
) {}