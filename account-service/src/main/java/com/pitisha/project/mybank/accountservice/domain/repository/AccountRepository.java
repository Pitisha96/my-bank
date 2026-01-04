package com.pitisha.project.mybank.accountservice.domain.repository;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID>, JpaSpecificationExecutor<AccountEntity> {

    Optional<AccountEntity> findByNumber(UUID number);

    List<AccountEntity> findByOwnerId(UUID ownerId);

    @Lock(PESSIMISTIC_WRITE)
    @Query("SELECT acc FROM AccountEntity acc WHERE acc.number = :number")
    Optional<AccountEntity> findByNumberForUpdate(@Param("number") UUID number);
}
