package com.pitisha.project.mybank.notificationservice.domain.service;

import com.pitisha.project.mybank.notificationservice.api.dto.response.NotificationMessage;
import com.pitisha.project.mybank.notificationservice.domain.exception.ResourceNotFoundException;
import com.pitisha.project.mybank.notificationservice.domain.mapper.NotificationMapper;
import com.pitisha.project.mybank.notificationservice.domain.entity.NotificationType;
import com.pitisha.project.mybank.notificationservice.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final String NOTIFICATION_NOT_FOUND_MESSAGE = "Notification with id %s not found";
    private static final String RECIPIENT_IS_UNAUTHORIZED_MESSAGE = "Recipient is unauthorized";
    private static final String FORBIDDEN_MESSAGE = "you don't have permissions to read this notification";
    private static final String NOTIFICATION_DESTINATION = "/queue/notifications";

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationMessage> findByRecipientAndStatus(final UUID recipientId, final Boolean delivered) {
        if (isNull(recipientId)) {
            return emptyList();
        }
        return notificationRepository.findByRecipientIdAndDelivered(recipientId, nonNull(delivered) && delivered)
            .stream()
            .map(notificationMapper::toNotificationMessage).toList();
    }

    @Override
    @Transactional
    public void add(final UUID txId, final NotificationType type, final UUID recipientId, final String message) {
        final var id = notificationRepository.insertIfNotExists(txId, type.name(), recipientId, message, false);
        if (id.isEmpty()) {
            return;
        }
        messagingTemplate.convertAndSendToUser(
            recipientId.toString(),
            NOTIFICATION_DESTINATION,
            new NotificationMessage(id.get(), type, message)
        );
    }

    @Override
    @Transactional
    public void read(final Long id, final UUID recipientId) {
        final var entity = notificationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(NOTIFICATION_NOT_FOUND_MESSAGE.formatted(id)));
        if (isNull(recipientId)) {
            throw new InsufficientAuthenticationException(RECIPIENT_IS_UNAUTHORIZED_MESSAGE);
        }
        if (!recipientId.equals(entity.getRecipientId())) {
            throw new AccessDeniedException(FORBIDDEN_MESSAGE);
        }
        entity.setDelivered(true);
        notificationRepository.save(entity);
    }
}
