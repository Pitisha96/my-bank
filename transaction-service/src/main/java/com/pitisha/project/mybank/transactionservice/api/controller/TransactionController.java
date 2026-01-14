package com.pitisha.project.mybank.transactionservice.api.controller;

import static com.pitisha.project.mybank.domain.entity.AccountCurrency.valueOf;
import static org.springframework.http.ResponseEntity.accepted;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import com.pitisha.project.mybank.transactionservice.api.dto.request.OneAccountOperation;
import com.pitisha.project.mybank.transactionservice.api.dto.request.TwoAccountsOperation;
import com.pitisha.project.mybank.transactionservice.api.dto.response.TransactionResponse;
import com.pitisha.project.mybank.transactionservice.domain.exception.ResourceNotFoundException;
import com.pitisha.project.mybank.transactionservice.domain.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController extends AbstractController {

    private static final String ROOT_PATH = "/api/v1/transactions";
    private static final String TRANSACTION_IS_NOT_DEFINED = "Transaction is not defined";

    private final TransactionService transactionService;

    @GetMapping("/{txId}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable final UUID txId) {
        return transactionService.findById(txId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_IS_NOT_DEFINED));
    }

    @PostMapping("/deposit")
    public ResponseEntity<UUID> deposit(final JwtAuthenticationToken token,
                                        @Validated @RequestBody final OneAccountOperation request) {
        final UUID id = transactionService.startDeposit(
                getCurrentUserIdFromToken(token),
                request.accountId(),
                request.amount(),
                valueOf(request.currency())
        );
        final var location = fromCurrentRequest()
                .replacePath(null)
                .path(ROOT_PATH)
                .path(ID_PATH_VARIABLE)
                .buildAndExpand(id)
                .toUri();
        return accepted()
                .location(location)
                .build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<UUID> withdraw(final JwtAuthenticationToken token,
                                         @Validated @RequestBody final OneAccountOperation request) {
        final UUID id = transactionService.startWithdraw(
                getCurrentUserIdFromToken(token),
                request.accountId(),
                request.amount(),
                valueOf(request.currency())
        );
        final var location = fromCurrentRequest()
                .replacePath(null)
                .path(ROOT_PATH)
                .path(ID_PATH_VARIABLE)
                .buildAndExpand(id)
                .toUri();
        return accepted()
                .location(location)
                .build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<UUID> transfer(final JwtAuthenticationToken token,
                                         @Validated @RequestBody final TwoAccountsOperation req) {
        final UUID id = transactionService.startTransfer(
                getCurrentUserIdFromToken(token),
                req.fromAccountId(),
                req.toAccountId(),
                req.amount(),
                valueOf(req.currency())
        );
        final var location = fromCurrentRequest()
                .replacePath(null)
                .path(ROOT_PATH)
                .path(ID_PATH_VARIABLE)
                .buildAndExpand(id)
                .toUri();
        return accepted()
                .location(location)
                .build();
    }
}
