package com.pitisha.project.accountservice.api.controller;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

import com.pitisha.project.accountservice.api.dto.request.AmountRequest;
import com.pitisha.project.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.accountservice.api.dto.request.CreateSelfAccountRequest;
import com.pitisha.project.accountservice.domain.entity.AccountCurrency;
import com.pitisha.project.accountservice.domain.service.AccountService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/accounts")
public class UserAccountController extends AbstractAccountController {

    public UserAccountController(final AccountService accountService) {
        super(accountService);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ResponseEntity<List<AccountResponse>> findMyAccounts(final JwtAuthenticationToken token) {
        final var userId = getCurrentUserIdFromToken(token);
        return ok(accountService.findByOwnerId(userId));
    }

    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping("/self")
    public ResponseEntity<AccountResponse> createSelfAccount(JwtAuthenticationToken token,
                                                             @RequestBody @Validated final CreateSelfAccountRequest request) {
        final var userId = getCurrentUserIdFromToken(token);
        final AccountResponse resp = accountService.create(userId, AccountCurrency.valueOf(request.currency()));
        return created(createLocation(resp.number())).body(resp);
    }

    @PreAuthorize("hasAnyRole('USER')")
    @PatchMapping("/{number}/deposit")
    public ResponseEntity<AccountResponse> deposit(@PathVariable final UUID number, @RequestBody final AmountRequest request) {
        return ok(accountService.deposit(number, request.amount()));
    }

    @PreAuthorize("hasAnyRole('USER')")
    @PatchMapping("/{number}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable final UUID number, @RequestBody final AmountRequest request) {
        return ok(accountService.withdraw(number, request.amount()));
    }
}
