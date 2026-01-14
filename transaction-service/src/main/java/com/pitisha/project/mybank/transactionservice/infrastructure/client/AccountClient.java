package com.pitisha.project.mybank.transactionservice.infrastructure.client;

import com.pitisha.project.mybank.transactionservice.infrastructure.client.dto.request.AmountRequest;
import com.pitisha.project.mybank.transactionservice.config.FeignClientConfig;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(
        name = "account-service",
        url = "http://localhost:8081/api/v1/internal",
        configuration = FeignClientConfig.class
)
public interface AccountClient {

    @PostMapping("/accounts/{accountId}/operations/{txId}/reserve")
    @Retry(name = "accountClient")
    void reserve(@RequestHeader("X-User-Id") String userId,
                 @PathVariable("accountId") UUID accountId,
                 @PathVariable("txId") UUID txId,
                 @RequestBody AmountRequest body);

    @PostMapping("/operations/{txId}/withdraw")
    @Retry(name = "accountClient")
    void withdraw(@PathVariable("txId") UUID txId);

    @PostMapping("/operations/{txId}/cancel")
    @Retry(name = "accountClient")
    void cancel(@PathVariable("txId") UUID txId);

    @PostMapping("/accounts/{accountId}/operations/{txId}/credit")
    @Retry(name = "accountClient")
    void credit(@RequestHeader("X-User-Id") String userId,
                @PathVariable("accountId") UUID accountId,
                @PathVariable("txId") UUID txId,
                @RequestBody AmountRequest body);
}
