package com.pitisha.project.mybank.transactionservice.config;

import static feign.Logger.Level.BASIC;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
@Configuration
@EnableFeignClients(basePackages = "com.pitisha.project.mybank.transactionservice.infrastructure.client")
public class FeignClientConfig {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer %s";
    private static final String KEYCLOAK_REGISTRATION_ID = "keycloak";

    @Value("${TX_SERVICE_CLIENT_ID}")
    private String serviceClientId;

    @Bean
    public Logger.Level feignLoggerLevel() {
        return BASIC;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(2L, SECONDS, 3L, SECONDS, true);
    }

    @Bean
    RequestInterceptor oauth2AuthenticationRequestInterceptor(final OAuth2AuthorizedClientManager authorizedClientManager) {
        return requestTemplate -> {
            final OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                    .withClientRegistrationId(KEYCLOAK_REGISTRATION_ID)
                    .principal(serviceClientId)
                    .build();

            final OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(request);
            if (isNull(authorizedClient) || isNull(authorizedClient.getAccessToken())) {
                return;
            }
            requestTemplate.header(
                    AUTHORIZATION_HEADER,
                    BEARER_PREFIX.formatted(authorizedClient.getAccessToken().getTokenValue())
            );
        };
    }
}
