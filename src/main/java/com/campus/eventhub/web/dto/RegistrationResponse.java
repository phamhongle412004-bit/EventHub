package com.campus.eventhub.web.dto;

import com.campus.eventhub.domain.RegistrationStatus;
import java.time.LocalDateTime;

public record RegistrationResponse(
        Long id,
        Long eventId,
        String eventTitle,
        Long participantId,
        String participantName,
        LocalDateTime registeredAt,
        RegistrationStatus status
) {}