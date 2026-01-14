package com.pitisha.project.mybank.transactionservice.domain.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.UUID;
import static java.time.LocalDateTime.now;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
@Getter
@Setter
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = UUID)
    @Column(nullable = false, unique = true)
    private UUID id;

    private UUID initiator;

    @Column(nullable = false, length = 15)
    @Enumerated(STRING)
    private TransactionType type;

    @Column(nullable = false, length = 15)
    @Enumerated(STRING)
    private TransactionStatus status;

    private UUID fromAccountId;

    private UUID toAccountId;

    @Positive
    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Enumerated(STRING)
    private AccountCurrency currency;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public TransactionEntity(final UUID initiator,
                             final TransactionType type,
                             final TransactionStatus status,
                             final UUID fromAccountId,
                             final UUID toAccountId,
                             final BigDecimal amount,
                             final AccountCurrency currency) {
        this.initiator = initiator;
        this.type = type;
        this.status = status;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.currency = currency;
    }

    @PrePersist
    private void onCreate() {
        createdAt = now();
        updatedAt = now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = now();
    }
}
