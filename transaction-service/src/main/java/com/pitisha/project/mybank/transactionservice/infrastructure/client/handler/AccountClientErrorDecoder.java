package com.pitisha.project.mybank.transactionservice.infrastructure.client.handler;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.pitisha.project.mybank.transactionservice.infrastructure.client.dto.response.ErrorResponse;
import com.pitisha.project.mybank.transactionservice.infrastructure.client.exception.AccountServiceBusinessException;
import com.pitisha.project.mybank.transactionservice.infrastructure.client.exception.AccountServiceTechnicalException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@Slf4j
public class AccountClientErrorDecoder implements ErrorDecoder {

    private static final String UNDEFINED_ERROR = "Undefined account-service response error";

    private final ErrorDecoder errorDecoder = new Default();
    private final JsonMapper jsonMapper = new JsonMapper();

    @Override
    public Exception decode(final String key, final Response response) {
        final int status = response.status();
        if (status == 401 || status == 403) {
            return errorDecoder.decode(key, response);
        }
        if (status >= 500) {
            return new AccountServiceTechnicalException();
        }
        final Response.Body body = response.body();
        try (var is = nonNull(body) ? body.asInputStream() : null) {
            if (isNull(is)) {
                return new AccountServiceTechnicalException(UNDEFINED_ERROR);
            }
            final var error = jsonMapper.readValue(is, ErrorResponse.class);
            if (nonNull(error.errorCode())) {
                return new AccountServiceBusinessException(error.message());
            }
            return new AccountServiceTechnicalException(error.message());
        } catch (Exception e) {
            log.error(UNDEFINED_ERROR, e);
            return errorDecoder.decode(key, response);
        }
    }
}
