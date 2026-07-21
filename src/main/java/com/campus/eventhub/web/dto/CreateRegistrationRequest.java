package com.campus.eventhub.web.dto;

import jakarta.validation.constraints.NotNull;

public record CreateRegistrationRequest(
        @NotNull(message = "ID người tham gia là bắt buộc")
        Long participantId
) {}