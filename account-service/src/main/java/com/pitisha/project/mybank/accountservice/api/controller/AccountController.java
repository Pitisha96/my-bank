package com.pitisha.project.mybank.accountservice.api.controller;

import com.pitisha.project.mybank.accountservice.api.dto.request.AccountFilter;
import com.pitisha.project.mybank.accountservice.api.dto.request.CreateAccountRequest;
import com.pitisha.project.mybank.accountservice.api.dto.request.UpdateStatusRequest;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountPageResponse;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.mybank.accountservice.domain.exception.ResourceNotFoundException;
import com.pitisha.project.mybank.accountservice.domain.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private static final String ACCOUNT_WITH_ID_IS_NOT_DEFINED = "account with id %s is not defined";
    private static final String ID_PATH_VARIABLE = "/{id}";

    private final AccountService accountService;

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #params.ownerId() != null and #params.ownerId().toString() == principal.claims['sub'])")
    @GetMapping
    public ResponseEntity<AccountPageResponse> findAccounts(@Validated final AccountFilter params) {
        return ok(accountService.findAll(params));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or (hasRole('USER') and returnObject.ownerId() != null and returnObject.ownerId().toString() == principal.claims['sub'])")
    @GetMapping("/{id}")
    @ResponseStatus(OK)
    public AccountResponse findAccountById(@PathVariable final UUID id) {
        return accountService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_WITH_ID_IS_NOT_DEFINED.formatted(id)));
    }

    @PreAuthorize("hasRole('ADMIN') or (#request.ownerId() != null and #request.ownerId().toString() == principal.claims['sub'])")
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Validated @RequestBody final CreateAccountRequest request) {
        final AccountResponse response = accountService.create(request.ownerId(), request.currency());
        return created(createLocation(response.id())).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponse> updateStatus(@PathVariable final UUID id,
                                                        @Validated @RequestBody final UpdateStatusRequest request) {
        return ok(accountService.updateStatus(id, request.status()));
    }

    private URI createLocation(final UUID id) {
        return fromCurrentRequest()
            .path(ID_PATH_VARIABLE)
            .buildAndExpand(id)
            .toUri();
    }
}
