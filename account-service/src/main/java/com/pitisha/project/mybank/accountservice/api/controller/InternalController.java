package com.pitisha.project.mybank.accountservice.api.controller;

import static org.springframework.http.ResponseEntity.accepted;

import com.pitisha.project.mybank.accountservice.api.dto.request.AmountRequest;
import com.pitisha.project.mybank.accountservice.domain.service.AccountOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalController {

    private final AccountOperationService accountOperationService;

    @PostMapping("/accounts/{number}/operations/{transactionId}/reserve")
    public ResponseEntity<Void> reserve(@PathVariable final UUID number,
                                        @PathVariable final UUID transactionId,
                                        @Validated @RequestBody final AmountRequest request) {
        accountOperationService.reserve(transactionId, number, request.amount(), request.currency());
        return accepted().build();
    }

    @PostMapping("/operations/{transactionId}/withdraw")
    public ResponseEntity<Void> withdraw(@PathVariable final UUID transactionId) {
        accountOperationService.withdraw(transactionId);
        return accepted().build();
    }

    @PostMapping("/operations/{transactionId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable final UUID transactionId) {
        accountOperationService.cancel(transactionId);
        return accepted().build();
    }

    @PostMapping("/accounts/{number}/operations/{transactionId}/credit")
    public ResponseEntity<Void> credit(@PathVariable final UUID number,
                                       @PathVariable final UUID transactionId,
                                       @Validated @RequestBody final AmountRequest request) {
        accountOperationService.credit(transactionId, number, request.amount(), request.currency());
        return accepted().build();
    }
}
