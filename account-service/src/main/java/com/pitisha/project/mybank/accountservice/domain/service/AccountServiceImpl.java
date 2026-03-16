package com.pitisha.project.mybank.accountservice.domain.service;

import static com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus.ACTIVE;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus.CLOSED;
import static com.pitisha.project.mybank.accountservice.domain.entity.specification.AccountSpecification.withFilter;
import static com.pitisha.project.mybank.accountservice.domain.util.AccountNumberGenerator.generateAccountNumber;
import static com.pitisha.project.mybank.accountservice.domain.util.ArgumentValidationUtils.requireNonNullOrElseThrow;
import static com.pitisha.project.mybank.kafka.topic.TopicName.ACCOUNT_CREATED_TOPIC;

import com.pitisha.project.mybank.accountservice.api.dto.request.AccountFilter;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountPageResponse;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountEntity;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountOutboxEntity;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private static final String OWNER_ID_MUST_NOT_BE_NULL = "owner id must not be null";
    private static final String ACCOUNT_ID_MUST_NOT_BE_NULL = "account id must not be null";
    private static final String CURRENCY_MUST_NOT_BE_NULL = "account currency must not be null";
    private static final String STATUS_MUST_NOT_BE_NULL = "account status must not be null";
    private static final String ACCOUNT_WITH_ID_IS_NOT_DEFINED = "account with id %s is not defined";
    private static final String CANNOT_CLOSE_ACCOUNT_WITH_RESERVED_FUNDS = "cannot close account with reserved funds";
    private static final String ACCOUNT_ALREADY_CLOSED = "account is already closed";
    private final AccountRepository accountRepository;
    private final AccountsOutboxRepository outboxRepository;
    private final AccountMapper accountMapper;
    private final EncryptionService encryptionService;
    private final JsonMapper jsonMapper;

    @Value("${app.bik}")
    private String bik;

    @Cacheable(cacheNames = "accountPages", keyGenerator = "accountPageFilterKeyGenerator")
    @Transactional(readOnly = true)
    @Override
    public AccountPageResponse findAll(final AccountFilter filter) {
        final Page<AccountResponse> page = accountRepository.findAll(withFilter(filter), PageRequest.of(filter.page(), filter.pageSize()))
                .map(accountMapper::entityToDto);
        return new AccountPageResponse(page.getContent(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    @Cacheable(cacheNames = "findByIdAccounts", key = "#id")
    @Transactional(readOnly = true)
    @Override
    public Optional<AccountResponse> findById(final UUID id) {
        requireNonNullOrElseThrow(id, ACCOUNT_ID_MUST_NOT_BE_NULL);
        return accountRepository.findById(id)
            .map(accountMapper::entityToDto);
    }

    @CacheEvict(cacheNames = "accountPages", allEntries = true)
    @Transactional
    @Override
    public AccountResponse create(final UUID ownerId, final AccountCurrency currency) {
        requireNonNullOrElseThrow(ownerId, OWNER_ID_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(currency, CURRENCY_MUST_NOT_BE_NULL);
        final Long seq = accountRepository.getNextAccountNumber();
        final String generatedNumber = generateAccountNumber(bik, currency, seq);
        final AccountEntity entity = createAccount(generatedNumber, ownerId, currency);
        accountRepository.save(entity);
        final AccountOutboxEntity outbox = createAccountCreatedOutbox(entity);
        outboxRepository.save(outbox);
        return accountMapper.entityToDto(entity);
    }

    @Caching(
            evict = @CacheEvict(cacheNames = "accountPages", allEntries = true),
            put = @CachePut(cacheNames = "findByIdAccounts", key = "#id")
    )
    @Transactional
    @Override
    public AccountResponse updateStatus(final UUID id, final AccountStatus status) {
        requireNonNullOrElseThrow(id, ACCOUNT_ID_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(status, STATUS_MUST_NOT_BE_NULL);
        final var entity = accountRepository.findWithLockById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_WITH_ID_IS_NOT_DEFINED));
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

    private AccountEntity createAccount(final String number, final UUID ownerId, final AccountCurrency currency) {
        final var entity = new AccountEntity();
        entity.setNumber(number);
        entity.setNumberHash(encryptionService.generateHmac(number));
        entity.setOwnerId(ownerId);
        entity.setCurrency(currency);
        entity.setStatus(ACTIVE);
        return entity;
    }

    private AccountOutboxEntity createAccountCreatedOutbox(final AccountEntity entity) {
        final var event = new AccountCreatedEvent(
            entity.getId(),
            entity.getOwnerId(),
            entity.getCurrency()
        );
        final var outbox = new AccountOutboxEntity();
        outbox.setTopic(ACCOUNT_CREATED_TOPIC.getTopicName());
        outbox.setPayloadType(AccountCreatedEvent.class.getName());
        outbox.setPayload(jsonMapper.writeValueAsString(event));
        return outbox;
    }
}
