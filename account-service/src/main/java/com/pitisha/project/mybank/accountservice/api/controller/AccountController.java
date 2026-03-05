package com.pitisha.project.mybank.accountservice.api.controller;

import com.pitisha.project.mybank.accountservice.api.dto.request.AccountFilter;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountPageResponse;
import com.pitisha.project.mybank.accountservice.domain.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PreAuthorize("hasRole('ADMIN') or (#params.ownerId() != null and #params.ownerId().toString() == principal.claims['sub'])")
    @GetMapping
    public ResponseEntity<AccountPageResponse> findAccounts(@Validated final AccountFilter params) {
        return ok(accountService.findAll(params));
    }
}
