package com.campus.eventhub.web.dto;

import java.util.List;

public record UserResponseDto(
        String username,
        List<String> roles
) {}