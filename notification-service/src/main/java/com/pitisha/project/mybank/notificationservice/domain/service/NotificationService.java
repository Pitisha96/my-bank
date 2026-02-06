package com.pitisha.project.mybank.notificationservice.domain.service;

import com.pitisha.project.mybank.notificationservice.api.dto.response.NotificationMessage;
import com.pitisha.project.mybank.notificationservice.domain.entity.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationMessage> findByRecipientAndStatus(UUID recipientId, Boolean delivered);

    void add(UUID txId, NotificationType type, UUID recipientId, String message);

    void read(Long id, UUID recipientId);
}
