package com.campus.eventhub.web.dto;

import java.time.LocalDateTime;

public record ParticipantResponse(
        Long id,
        String fullName,
        String email,
        LocalDateTime createdAt
) {}