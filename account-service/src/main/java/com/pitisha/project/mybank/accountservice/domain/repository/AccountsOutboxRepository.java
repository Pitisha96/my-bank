package com.pitisha.project.mybank.accountservice.domain.repository;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountsOutboxRepository extends JpaRepository<AccountOutboxEntity, Long> {
    List<AccountOutboxEntity> findByProcessedFalse();
}
