package com.pitisha.project.accountservice.domain.repository;

import com.pitisha.project.accountservice.domain.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, Long>, JpaSpecificationExecutor<AccountEntity> {
    Optional<AccountEntity> findByNumber(UUID number);
    List<AccountEntity> findByOwnerId(UUID ownerId);
}
