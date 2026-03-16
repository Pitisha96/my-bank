package com.pitisha.project.mybank.accountservice.domain.repository;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID>, JpaSpecificationExecutor<AccountEntity> {

    @Lock(PESSIMISTIC_WRITE)
    Optional<AccountEntity> findWithLockById(UUID id);

    @Lock(PESSIMISTIC_WRITE)
    Optional<AccountEntity> findWithLockByNumberHash(String numberHash);

    @Query(value = "SELECT nextval('account_number_seq')", nativeQuery = true)
    Long getNextAccountNumber();
}
