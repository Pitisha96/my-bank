package com.pitisha.project.mybank.transactionservice.domain.repository;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionEntity;
import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
        SELECT tx FROM TransactionEntity tx WHERE tx.id = :txId
        """)
    Optional<TransactionEntity> lockForUpdate(@Param("txId") UUID transactionId);

    @Query("""
        SELECT tx.status FROM TransactionEntity tx WHERE tx.id = :txId
        """)
    Optional<TransactionStatus> findStatusById(@Param("txId") UUID transactionId);
}
