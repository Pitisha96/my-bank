package com.pitisha.project.accountservice.api.controller;

import static java.util.UUID.fromString;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import com.pitisha.project.accountservice.domain.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class AbstractAccountController {

    private static final String SUB = "sub";
    private static final String ID_PATH_VARIABLE = "/{number}";

    protected final AccountService accountService;

    protected URI createLocation(final UUID id) {
        return fromCurrentRequest()
                .path(ID_PATH_VARIABLE)
                .buildAndExpand(id)
                .toUri();
    }

    protected UUID getCurrentUserIdFromToken(final JwtAuthenticationToken token) {
        return fromString((String) token.getTokenAttributes().get(SUB));
    }

}
