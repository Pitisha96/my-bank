package com.pitisha.project.mybank.notificationservice.domain.repository;

import com.pitisha.project.mybank.notificationservice.domain.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByRecipientIdAndDelivered(UUID recipientId, boolean delivered);

    @Query(value = """
        INSERT INTO notifications(transaction_id, type, recipient_id, message, delivered)
        VALUES (:txId, :type, :recipientId, :message, :delivered)
        ON CONFLICT(transaction_id, type) DO NOTHING
        RETURNING id;
        """, nativeQuery = true)
    Optional<Long> insertIfNotExists(@Param("txId") UUID transactionId,
                                     @Param("type") String type,
                                     @Param("recipientId") UUID recipientId,
                                     @Param("message") String message,
                                     @Param("delivered") Boolean delivered);
}
