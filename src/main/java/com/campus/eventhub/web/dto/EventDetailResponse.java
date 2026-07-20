package com.campus.eventhub.web.dto;

import com.campus.eventhub.domain.EventStatus;
import java.time.LocalDateTime;

public record EventDetailResponse(
        Long id,
        String title,
        String description,
        String location,
        LocalDateTime startAt,
        Integer capacity,
        Integer availableSeats,
        EventStatus status,
        LocalDateTime createdAt
) {}