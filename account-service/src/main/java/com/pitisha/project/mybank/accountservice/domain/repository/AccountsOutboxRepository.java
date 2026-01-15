package com.pitisha.project.mybank.accountservice.domain.repository;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountsOutboxRepository extends JpaRepository<AccountOutboxEntity, Long> {

    @Query(value = """
        SELECT * FROM accounts_outbox
        ORDER BY created_at
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<AccountOutboxEntity> lockBatch(@Param("limit") final int limit);
}
