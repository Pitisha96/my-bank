package com.pitisha.project.accountservice.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        LocalDateTime timestamp,
        Integer status,
        String message,
        List<String> detailedMessages
) { }
