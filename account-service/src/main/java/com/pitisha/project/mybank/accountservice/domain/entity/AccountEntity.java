package com.pitisha.project.mybank.accountservice.domain.entity;

import static com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus.BLOCKED;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus.CLOSED;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.UUID;
import static java.math.BigDecimal.ZERO;
import static java.time.OffsetDateTime.now;

import com.pitisha.project.mybank.accountservice.domain.entity.converter.AccountNumberConverter;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = UUID)
    private UUID id;

    @Convert(converter = AccountNumberConverter.class)
    @Column(nullable = false)
    private String number;

    @Column(nullable = false, unique = true)
    private String numberHash;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 4)
    @Enumerated(STRING)
    private AccountCurrency currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal reserved = ZERO;

    @Column(nullable = false, length = 10)
    @Enumerated(STRING)
    private AccountStatus status;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    private void setCreationDateTime() {
        createdAt = now();
        updatedAt = now();
    }

    @PreUpdate
    private void setUpdateDateTime() {
        updatedAt = now();
    }

    public boolean canReserve(final BigDecimal amount) {
        return balance.subtract(reserved).compareTo(amount) >= 0;
    }

    public boolean canReserve() {
        return !BLOCKED.equals(status) && !CLOSED.equals(status);
    }

    public boolean canWithdraw() {
        return !BLOCKED.equals(status) && !CLOSED.equals(status);
    }

    public boolean canCancel() {
        return !CLOSED.equals(status);
    }

    public boolean canCredit() {
        return !BLOCKED.equals(status) && !CLOSED.equals(status);
    }
}
