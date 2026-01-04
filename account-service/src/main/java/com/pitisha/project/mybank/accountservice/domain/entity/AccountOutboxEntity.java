package com.pitisha.project.mybank.accountservice.domain.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static java.time.LocalDateTime.now;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonType;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts_outbox")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountOutboxEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String topic;

    @JdbcType(PostgreSQLJsonPGObjectJsonType.class)
    @Column(nullable = false, columnDefinition = "json")
    private String payload;

    @Column(nullable = false)
    private Boolean processed;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = now();
    }
}
