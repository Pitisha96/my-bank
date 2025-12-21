package com.pitisha.project.accountservice.domain.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.UUID;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private UUID number;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false,length = 16)
    @Enumerated(STRING)
    private AccountCurrency currency;

    @Column(nullable = false)
    private BigDecimal balance = ZERO;

    @Column(nullable = false, length = 7)
    @Enumerated(STRING)
    private AccountStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    private void setCreationDateTime() {
        createdAt = now();
        updatedAt = now();
    }

    @PreUpdate
    private void setUpdateDateTime() {
        updatedAt = now();
    }
}
