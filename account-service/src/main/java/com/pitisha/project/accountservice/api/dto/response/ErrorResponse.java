package com.pitisha.project.accountservice.api.dto.response;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        Integer status,
        String message,
        String detailedMessage
) { }
