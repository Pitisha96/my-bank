package com.pitisha.project.mybank.transactionservice.api.controller;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static java.util.UUID.fromString;
import static org.springframework.http.ResponseEntity.accepted;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import com.pitisha.project.mybank.transactionservice.api.dto.request.OneAccountOperation;
import com.pitisha.project.mybank.transactionservice.api.dto.request.TwoAccountsOperation;
import com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorResponse;
import com.pitisha.project.mybank.transactionservice.api.dto.response.TransactionResponse;
import com.pitisha.project.mybank.transactionservice.domain.exception.ResourceNotFoundException;
import com.pitisha.project.mybank.transactionservice.domain.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "transactions", description = "provides transaction management operations")
@SecurityRequirement(name = "keycloak", scopes = {"openid", "email", "profile"})
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "400",
                description = "Bad request look like invalid or missing parameter, invalid request body or validation error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "Invalid request",
                                summary = "Validation error",
                                value = """
                                 {
                                    "errorCode": "VALIDATION_ERROR",
                                    "message": "Validation error",
                                    "details": {
                                        "amount": "must equals or greater than 0"
                                    },
                                    "timestamp": "2026-01-13T13:05:10"
                                 }
                                 """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "User is unauthorized",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "User is unauthorized",
                                summary = "Unauthorized",
                                value = """
                                 {
                                    "errorCode": "UNAUTHORIZED",
                                    "message": "Unauthorized",
                                    "details": null,
                                    "timestamp": "2026-01-13T13:05:10"
                                 }
                                 """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Something went wrong",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "Something went wrong",
                                summary = "Internal server error",
                                value = """
                                 {
                                    "errorCode": "INTERNAL_SERVER_ERROR",
                                    "message": "Something went wrong",
                                    "details": null,
                                    "timestamp": "2026-01-13T13:05:10"
                                 }
                                 """
                        )
                )
        )
})
@RequiredArgsConstructor
public class TransactionController {

    private static final String ID_PATH_VARIABLE = "/{txId}";
    private static final String SUB_CLAIM = "sub";
    private static final String ROOT_PATH = "/api/v1/transactions";
    private static final String TRANSACTION_IS_NOT_DEFINED = "Transaction is not defined";

    private final TransactionService transactionService;

    @Operation(
            operationId = "findTransactionById",
            summary = "Find transaction by id",
            description = "Finds transaction by provided id"
    )
    @Parameters(value = @Parameter(
            name = "txId",
            in = PATH,
            description = "Transaction identifier",
            required = true
    ))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction has been provided",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Transaction is not defined",
                                    summary = "Resource not found",
                                    value = """
                                 {
                                    "errorCode": "RESOURCE_NOT_FOUND",
                                    "message": "Transaction is not defined",
                                    "details": null,
                                    "timestamp": "2026-01-13T13:05:10"
                                 }
                                 """
                            )
                    )
            )
    })
    @GetMapping("/{txId}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable final UUID txId) {
        return transactionService.findById(txId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_IS_NOT_DEFINED));
    }

    @Operation(
            operationId = "depositToAccount",
            summary = "Deposit to account provided amount",
            description = "Deposit amount to provided account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    description = "Deposit transaction has been accepted",
                    responseCode = "204",
                    headers = @Header(
                            name = "Location",
                            description = "URL of created transaction",
                            schema = @Schema(
                                    type = "string",
                                    format = "uri",
                                    example = "/api/v1/transactions/5ebbcf0e-7204-45b7-842c-b2b6072eb391"
                            )
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "string",
                                    format = "uuid",
                                    example = "5ebbcf0e-7204-45b7-842c-b2b6072eb391"
                            )
                    )
            )
    })
    @ResponseStatus
    @PostMapping("/deposit")
    public ResponseEntity<UUID> deposit(final JwtAuthenticationToken token,
                                        @Validated @RequestBody final OneAccountOperation request) {
        final UUID id = transactionService.startDeposit(
                getCurrentUserIdFromToken(token),
                request.accountId(),
                request.amount(),
                request.currency()
        );
        return accepted()
                .location(createLocation(id))
                .build();
    }

    @Operation(
            operationId = "withdrawFromAccount",
            summary = "Withdraw from account provided amount",
            description = "Withdraw amount from provided account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    description = "Withdraw transaction has been accepted",
                    responseCode = "204",
                    headers = @Header(
                            name = "Location",
                            description = "URL of created transaction",
                            schema = @Schema(
                                    type = "string",
                                    format = "uri",
                                    example = "/api/v1/transactions/5ebbcf0e-7204-45b7-842c-b2b6072eb391"
                            )
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "string",
                                    format = "uuid",
                                    example = "5ebbcf0e-7204-45b7-842c-b2b6072eb391"
                            )
                    )
            )
    })
    @PostMapping("/withdraw")
    public ResponseEntity<UUID> withdraw(final JwtAuthenticationToken token,
                                         @Validated @RequestBody final OneAccountOperation request) {
        final UUID id = transactionService.startWithdraw(
                getCurrentUserIdFromToken(token),
                request.accountId(),
                request.amount(),
                request.currency()
        );
        return accepted()
                .location(createLocation(id))
                .build();
    }

    @Operation(
            operationId = "transferFromAccountToAccount",
            summary = "Transfer from one account to another account provided amount",
            description = "Provides transfer operations between accounts"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    description = "Transfer transaction has been accepted",
                    responseCode = "204",
                    headers = @Header(
                            name = "Location",
                            description = "URL of created transaction",
                            schema = @Schema(
                                    type = "string",
                                    format = "uri",
                                    example = "/api/v1/transactions/5ebbcf0e-7204-45b7-842c-b2b6072eb391"
                            )
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "string",
                                    format = "uuid",
                                    example = "5ebbcf0e-7204-45b7-842c-b2b6072eb391"
                            )
                    )
            )
    })
    @PostMapping("/transfer")
    public ResponseEntity<UUID> transfer(final JwtAuthenticationToken token,
                                         @Validated @RequestBody final TwoAccountsOperation req) {
        final UUID id = transactionService.startTransfer(
                getCurrentUserIdFromToken(token),
                req.fromAccountId(),
                req.toAccountId(),
                req.amount(),
                req.currency()
        );
        return accepted()
                .location(createLocation(id))
                .build();
    }

    private URI createLocation(final UUID id) {
        return fromCurrentRequest()
                .replacePath(null)
                .path(ROOT_PATH)
                .path(ID_PATH_VARIABLE)
                .buildAndExpand(id)
                .toUri();
    }

    private UUID getCurrentUserIdFromToken(final JwtAuthenticationToken token) {
        return fromString((String) token.getTokenAttributes().get(SUB_CLAIM));
    }
}
