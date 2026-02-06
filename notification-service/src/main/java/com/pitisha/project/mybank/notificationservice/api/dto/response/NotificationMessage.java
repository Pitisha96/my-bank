package com.pitisha.project.mybank.notificationservice.api.dto.response;

import com.pitisha.project.mybank.notificationservice.domain.entity.NotificationType;

public record NotificationMessage(
    Long id,
    NotificationType type,
    String message
) { }
