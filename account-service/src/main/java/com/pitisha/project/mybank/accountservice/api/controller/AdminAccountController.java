package com.pitisha.project.mybank.accountservice.api.controller;

import static com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus.ACTIVE;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus.BLOCKED;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus.CLOSED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

import com.pitisha.project.mybank.accountservice.api.dto.request.AccountFilter;
import com.pitisha.project.mybank.accountservice.api.dto.request.CreateAccountRequest;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountPageResponse;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.mybank.accountservice.domain.service.AccountService;
import com.pitisha.project.mybank.accountservice.domain.exception.ResourceNotFoundException;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AdminAccountController extends AbstractAccountController {

    private static final String ACCOUNT_WITH_NUMBER_IS_NOT_DEFINED = "account with number %s is not defined";

    public AdminAccountController(final AccountService accountService) {
        super(accountService);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<AccountPageResponse> findAccounts(@Validated final AccountFilter params) {
        return ok(accountService.findAll(params));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{number}")
    public ResponseEntity<AccountResponse> findAccountByNumber(final @PathVariable UUID number) {
        return accountService.findByNumber(number)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_WITH_NUMBER_IS_NOT_DEFINED));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(final JwtAuthenticationToken token,
                                                         @RequestBody@Validated final CreateAccountRequest request) {
        if (request.ownerId().equals(getCurrentUserIdFromToken(token))) {
            return ResponseEntity.status(FORBIDDEN).build();
        }
        final AccountResponse resp = accountService.create(request.ownerId(), AccountCurrency.valueOf(request.currency()));
        return created(createLocation(resp.number())).body(resp);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{number}/activate")
    public ResponseEntity<AccountResponse> activateAccount(@PathVariable final UUID number) {
        return ok(accountService.updateStatus(number, ACTIVE));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{number}/block")
    public ResponseEntity<AccountResponse> blockAccount(@PathVariable final UUID number) {
        return ok(accountService.updateStatus(number, BLOCKED));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PatchMapping("/{number}/close")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable final UUID number) {
        return ok(accountService.updateStatus(number, CLOSED));
    }
}
