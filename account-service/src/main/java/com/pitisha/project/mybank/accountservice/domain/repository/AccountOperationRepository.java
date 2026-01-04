package com.pitisha.project.mybank.accountservice.domain.repository;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationEntity;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AccountOperationRepository extends JpaRepository<AccountOperationEntity, Long> {

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
        SELECT op FROM AccountOperationEntity op
        WHERE op.transactionId = :transactionId AND op.type = :type
        """)
    Optional<AccountOperationEntity> findByTxIdAndTypeForUpdate(@Param("transactionId") UUID txId,
                                                                @Param("type") AccountOperationType type);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT INTO account_operations(transaction_id, type, account_id, amount)
        VALUES (:transactionId, :type, :accountId, :amount)
        ON CONFLICT DO NOTHING;
        """, nativeQuery = true)
    int insertIfNotExists(@Param("transactionId") UUID txId,
                          @Param("type") String type,
                          @Param("accountId") UUID accountId,
                          @Param("amount") BigDecimal amount);
}
