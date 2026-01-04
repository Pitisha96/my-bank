package com.pitisha.project.mybank.accountservice.api.controller;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.mybank.accountservice.api.dto.request.CreateSelfAccountRequest;
import com.pitisha.project.mybank.accountservice.domain.service.AccountService;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<AccountResponse> createSelfAccount(final JwtAuthenticationToken token,
                                                             @RequestBody @Validated final CreateSelfAccountRequest request) {
        final var userId = getCurrentUserIdFromToken(token);
        final AccountResponse resp = accountService.create(userId, AccountCurrency.valueOf(request.currency()));
        return created(createLocation(resp.number())).body(resp);
    }
}
