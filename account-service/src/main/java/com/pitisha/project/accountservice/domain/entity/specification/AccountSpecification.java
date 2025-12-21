package com.pitisha.project.accountservice.domain.entity.specification;

import static java.time.LocalTime.MAX;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.pitisha.project.accountservice.api.dto.request.AccountFilter;
import com.pitisha.project.accountservice.domain.entity.AccountEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class AccountSpecification {

    private static final String OWNER_ID = "ownerId";
    private static final String CURRENCY = "currency";
    private static final String BALANCE = "balance";
    private static final String STATUS = "status";
    private static final String CREATED_AT = "createdAt";

    public static Specification<AccountEntity> withFilter(final AccountFilter filter) {
        Specification<AccountEntity> combinedSpec = (root, query, criteriaBuilder) -> null;
        if (nonNull(filter.ownerId())) {
            combinedSpec = combinedSpec.and(ownerIdEquals(filter.ownerId()));
        }
        if (isNotBlank(filter.currency())) {
            combinedSpec = combinedSpec.and(currencyEquals(filter.currency()));
        }
        if (nonNull(filter.balanceFrom())) {
            combinedSpec = combinedSpec.and(balanceGreaterThanOrEquals(filter.balanceFrom()));
        }
        if (nonNull(filter.balanceTo())) {
            combinedSpec = combinedSpec.and(balanceLessThanOrEquals(filter.balanceTo()));
        }
        if (isNotBlank(filter.status())) {
            combinedSpec = combinedSpec.and(statusEquals(filter.status()));
        }
        if (nonNull(filter.createdFrom())) {
            combinedSpec = combinedSpec.and(createdAtGreaterThanOrEquals(filter.createdFrom()));
        }
        if (nonNull(filter.createdTo())) {
            combinedSpec = combinedSpec.and(createdAtLessThanOrEquals(filter.createdTo()));
        }
        return combinedSpec;
    }

    public static Specification<AccountEntity> ownerIdEquals(final UUID userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OWNER_ID), userId);
    }

    public static Specification<AccountEntity> currencyEquals(final String currency) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(CURRENCY), currency);
    }

    public static Specification<AccountEntity> balanceGreaterThanOrEquals(final BigDecimal balance) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(BALANCE), balance);
    }

    public static Specification<AccountEntity> balanceLessThanOrEquals(final BigDecimal balance) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(BALANCE), balance);
    }

    public static Specification<AccountEntity> statusEquals(final String status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(STATUS), status);
    }

    public static Specification<AccountEntity> createdAtGreaterThanOrEquals(final LocalDate createdAt) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(CREATED_AT), createdAt.atStartOfDay());
    }

    public static Specification<AccountEntity> createdAtLessThanOrEquals(final LocalDate createdAt) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(CREATED_AT), createdAt.atTime(MAX));
    }
}
