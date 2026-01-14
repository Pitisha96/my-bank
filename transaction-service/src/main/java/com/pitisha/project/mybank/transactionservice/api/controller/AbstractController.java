package com.pitisha.project.mybank.transactionservice.api.controller;

import static java.util.UUID.fromString;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

public abstract class AbstractController {

    protected static final String ID_PATH_VARIABLE = "/{txId}";
    protected static final String SUB_CLAIM = "sub";

    protected UUID getCurrentUserIdFromToken(final JwtAuthenticationToken token) {
        return fromString((String) token.getTokenAttributes().get(SUB_CLAIM));
    }
}
