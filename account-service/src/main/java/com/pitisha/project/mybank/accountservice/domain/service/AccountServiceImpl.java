package com.pitisha.project.mybank.accountservice.domain.service;

import static com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus.ACTIVE;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus.CLOSED;
import static com.pitisha.project.mybank.accountservice.domain.entity.specification.AccountSpecification.withFilter;
import static com.pitisha.project.mybank.accountservice.domain.util.ArgumentValidationUtils.requireNonNullOrElseThrow;
import static com.pitisha.project.mybank.kafka.topic.TopicName.ACCOUNT_CREATED_TOPIC;
import static java.util.Objects.compare;
import static java.util.Objects.nonNull;

import com.pitisha.project.mybank.accountservice.api.dto.request.AccountFilter;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountPageResponse;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountEntity;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountOutboxEntity;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus;
import com.pitisha.project.mybank.accountservice.domain.exception.ValidationException;
import com.pitisha.project.mybank.accountservice.domain.repository.AccountRepository;
import com.pitisha.project.mybank.accountservice.domain.repository.AccountsOutboxRepository;
import com.pitisha.project.mybank.accountservice.domain.exception.IllegalBalanceStateException;
import com.pitisha.project.mybank.accountservice.domain.exception.IllegalStatusStateException;
import com.pitisha.project.mybank.accountservice.domain.exception.ResourceNotFoundException;
import com.pitisha.project.mybank.accountservice.domain.mapper.AccountMapper;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import com.pitisha.project.mybank.kafka.event.AccountCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private static final String OWNER_ID_MUST_NOT_BE_NULL = "owner id must not be null";
    private static final String ACCOUNT_NUMBER_MUST_NOT_BE_NULL = "account number must not be null";
    private static final String CURRENCY_MUST_NOT_BE_NULL = "account currency must not be null";
    private static final String STATUS_MUST_NOT_BE_NULL = "account status must not be null";
    private static final String ACCOUNT_WITH_NUMBER_IS_NOT_DEFINED = "account with number %s is not defined";
    private static final String BALANCE_ERROR_MESSAGE = "balanceFrom must not be greater than balanceTo";
    private static final String CREATED_ERROR_MESSAGE = "createdFrom must not be after createdTo";
    private static final String CANNOT_CLOSE_ACCOUNT_WITH_RESERVED_FUNDS = "cannot close account with reserved funds";
    private static final String ACCOUNT_ALREADY_CLOSED = "account is already closed";
    private final AccountRepository accountRepository;
    private final AccountsOutboxRepository outboxRepository;
    private final AccountMapper accountMapper;
    private final JsonMapper jsonMapper;

    @Cacheable(cacheNames = "accountPages", keyGenerator = "accountPageFilterKeyGenerator")
    @Transactional(readOnly = true)
    @Override
    public AccountPageResponse findAll(final AccountFilter filter) {
        validateFilter(filter);
        final Page<AccountResponse> page = accountRepository.findAll(withFilter(filter), PageRequest.of(filter.page(), filter.pageSize()))
                .map(accountMapper::entityToDto);
        return new AccountPageResponse(page.getContent(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    @Cacheable(cacheNames = "findByNumberAccounts", key = "#number")
    @Transactional(readOnly = true)
    @Override
    public Optional<AccountResponse> findByNumber(final UUID number) {
        requireNonNullOrElseThrow(number, ACCOUNT_NUMBER_MUST_NOT_BE_NULL);
        return accountRepository.findByNumber(number)
                .map(accountMapper::entityToDto);
    }

    @Cacheable(cacheNames = "findByOwnerIdAccounts", key = "#ownerId")
    @Transactional(readOnly = true)
    @Override
    public List<AccountResponse> findByOwnerId(final UUID ownerId) {
        requireNonNullOrElseThrow(ownerId, OWNER_ID_MUST_NOT_BE_NULL);
        return accountRepository.findByOwnerId(ownerId).stream()
                .map(accountMapper::entityToDto)
                .toList();
    }

    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "accountPages", allEntries = true),
                    @CacheEvict(cacheNames = "findByOwnerIdAccounts", key = "#ownerId")
            }
    )
    @Transactional
    @Override
    public AccountResponse create(final UUID ownerId, final AccountCurrency currency) {
        requireNonNullOrElseThrow(ownerId, OWNER_ID_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(currency, CURRENCY_MUST_NOT_BE_NULL);
        final var entity = new AccountEntity();
        entity.setOwnerId(ownerId);
        entity.setCurrency(currency);
        entity.setStatus(ACTIVE);
        accountRepository.save(entity);

        //outbox process
        final var outbox = new AccountOutboxEntity();
        outbox.setTopic(ACCOUNT_CREATED_TOPIC.getTopicName());
        outbox.setPayload(jsonMapper.writeValueAsString(
                new AccountCreatedEvent(
                        entity.getNumber(),
                        entity.getOwnerId(),
                        entity.getCurrency().toString()
                )
        ));
        outbox.setProcessed(false);
        outboxRepository.save(outbox);

        return accountMapper.entityToDto(entity);
    }

    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "accountPages", allEntries = true),
                    @CacheEvict(cacheNames = "findByOwnerIdAccounts", key = "#result.ownerId()")
            },
            put = {
                    @CachePut(cacheNames = "findByNumberAccounts", key = "#number")
            }
    )
    @Transactional
    @Override
    public AccountResponse updateStatus(final UUID number, final AccountStatus status) {
        requireNonNullOrElseThrow(number, ACCOUNT_NUMBER_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(status, STATUS_MUST_NOT_BE_NULL);
        final var entity = accountRepository.findByNumberForUpdate(number)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_WITH_NUMBER_IS_NOT_DEFINED));
        if (status.equals(entity.getStatus())) {
            return accountMapper.entityToDto(entity);
        }
        if (CLOSED.equals(entity.getStatus())) {
            throw new IllegalStatusStateException(ACCOUNT_ALREADY_CLOSED);
        }
        if (CLOSED.equals(status) && entity.getReserved().signum() > 0) {
            throw new IllegalBalanceStateException(CANNOT_CLOSE_ACCOUNT_WITH_RESERVED_FUNDS);
        }
        entity.setStatus(status);
        return accountMapper.entityToDto(accountRepository.save(entity));
    }

    private void validateFilter(final AccountFilter filter) {
        final var balanceFrom = filter.balanceFrom();
        final var balanceTo = filter.balanceTo();
        final var createdFrom = filter.createdFrom();
        final var createdTo = filter.createdTo();
        if (nonNull(balanceFrom) && nonNull(balanceTo) && compare(balanceFrom, balanceTo, BigDecimal::compareTo) > 0) {
            throw new ValidationException(BALANCE_ERROR_MESSAGE);
        }
        if (nonNull(createdFrom) && nonNull(createdTo) && compare(createdFrom, createdTo, LocalDate::compareTo) > 0) {
            throw new ValidationException(CREATED_ERROR_MESSAGE);
        }
    }
}
