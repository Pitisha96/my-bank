package com.pitisha.project.mybank.accountservice.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

import static com.pitisha.project.mybank.accountservice.api.dto.response.ErrorResponse.forbidden;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    final JsonMapper jsonMapper = new JsonMapper();
    final BearerTokenAccessDeniedHandler delegate = new BearerTokenAccessDeniedHandler();

    @Override
    public void handle(final HttpServletRequest request,
                       final HttpServletResponse response,
                       final AccessDeniedException accessDeniedException) throws IOException {
        delegate.handle(request, response, accessDeniedException);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.getWriter().write(jsonMapper.writeValueAsString(forbidden()));
    }
}
