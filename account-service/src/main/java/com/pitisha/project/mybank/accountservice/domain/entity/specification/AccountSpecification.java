package com.pitisha.project.mybank.accountservice.domain.entity.specification;

import static java.util.Objects.nonNull;

import com.pitisha.project.mybank.accountservice.api.dto.request.AccountFilter;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountEntity;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AccountSpecification {

    private static final String OWNER_ID = "ownerId";
    private static final String CURRENCY = "currency";
    private static final String BALANCE = "balance";
    private static final String STATUS = "status";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";

    public static Specification<AccountEntity> withFilter(final AccountFilter filter) {
        Specification<AccountEntity> combinedSpec = (root, query, criteriaBuilder) -> null;
        if (nonNull(filter.ownerId())) {
            combinedSpec = combinedSpec.and(ownerIdEquals(filter.ownerId()));
        }
        if (nonNull(filter.currency())) {
            combinedSpec = combinedSpec.and(currencyEquals(filter.currency()));
        }
        if (nonNull(filter.balanceFrom())) {
            combinedSpec = combinedSpec.and(balanceGreaterThanOrEquals(filter.balanceFrom()));
        }
        if (nonNull(filter.balanceTo())) {
            combinedSpec = combinedSpec.and(balanceLessThanOrEquals(filter.balanceTo()));
        }
        if (nonNull(filter.status())) {
            combinedSpec = combinedSpec.and(statusEquals(filter.status()));
        }
        if (nonNull(filter.createdFrom())) {
            combinedSpec = combinedSpec.and(createdAtGreaterThanOrEquals(filter.createdFrom()));
        }
        if (nonNull(filter.createdTo())) {
            combinedSpec = combinedSpec.and(createdAtLessThanOrEquals(filter.createdTo()));
        }
        if (nonNull(filter.updatedFrom())) {
            combinedSpec = combinedSpec.and(updatedAtGreaterThanOrEquals(filter.updatedFrom()));
        }
        if (nonNull(filter.updatedTo())) {
            combinedSpec = combinedSpec.and(updatedAtLessThanOrEquals(filter.updatedTo()));
        }
        return combinedSpec;
    }

    public static Specification<AccountEntity> ownerIdEquals(final UUID userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OWNER_ID), userId);
    }

    public static Specification<AccountEntity> currencyEquals(final AccountCurrency currency) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(CURRENCY), currency.name());
    }

    public static Specification<AccountEntity> balanceGreaterThanOrEquals(final BigDecimal balance) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(BALANCE), balance);
    }

    public static Specification<AccountEntity> balanceLessThanOrEquals(final BigDecimal balance) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(BALANCE), balance);
    }

    public static Specification<AccountEntity> statusEquals(final AccountStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(STATUS), status.name());
    }

    public static Specification<AccountEntity> createdAtGreaterThanOrEquals(final OffsetDateTime createdAt) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(CREATED_AT), createdAt);
    }

    public static Specification<AccountEntity> createdAtLessThanOrEquals(final OffsetDateTime createdAt) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(CREATED_AT), createdAt);
    }

    public static Specification<AccountEntity> updatedAtGreaterThanOrEquals(final OffsetDateTime updatedAtAt) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThanOrEqualTo(root.get(UPDATED_AT), updatedAtAt);
    }

    public static Specification<AccountEntity> updatedAtLessThanOrEquals(final OffsetDateTime updatedAtAt) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.lessThanOrEqualTo(root.get(UPDATED_AT), updatedAtAt);
    }
}
