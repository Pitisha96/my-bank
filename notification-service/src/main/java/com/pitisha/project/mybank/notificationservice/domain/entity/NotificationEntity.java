package com.pitisha.project.mybank.notificationservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(
    name = "notifications",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_notifications_transaction_id_type",
        columnNames = {"transaction_id", "type"}
    )
)
@NoArgsConstructor
@Getter
@Setter
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID transactionId;

    @Column(nullable = false, length = 15)
    @Enumerated(STRING)
    private NotificationType type;

    @Column(nullable = false)
    private UUID recipientId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean delivered;
}
