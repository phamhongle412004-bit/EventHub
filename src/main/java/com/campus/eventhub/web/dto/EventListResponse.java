package com.campus.eventhub.web.dto;

import com.campus.eventhub.domain.EventStatus;
import java.time.LocalDateTime;

public record EventListResponse(
        Long id,
        String title,
        LocalDateTime startAt,
        Integer availableSeats,
        EventStatus status
) {}