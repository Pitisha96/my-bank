package com.pitisha.project.mybank.accountservice.domain.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.time.LocalDateTime.now;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "account_operations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_account_operations_transaction_id_type",
                columnNames = {"transaction_id", "type"}
        ),
        indexes = @Index(
                name = "ux_account_operations_transaction_id",
                columnList = "transaction_id",
                unique = true
        )
)
@NoArgsConstructor
@Getter
@Setter
public class AccountOperationEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false)
    private UUID transactionId;

    @Enumerated(STRING)
    @Column(nullable = false, length = 20)
    private AccountOperationType type;

    @Column(nullable = false)
    private UUID accountId;

    @Positive
    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = now();
    }
}
