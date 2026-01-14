package com.pitisha.project.mybank.transactionservice.infrastructure.client.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        ErrorCode errorCode,
        String message,
        Map<String, Object> details,
        LocalDateTime timestamp
) { }
