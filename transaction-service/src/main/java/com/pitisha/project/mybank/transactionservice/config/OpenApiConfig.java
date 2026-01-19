package com.pitisha.project.mybank.transactionservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transaction Service API")
                        .version("0.2.0")
                        .description("Provided remote transaction service API")
                        .contact(new Contact()
                                .name("Ilya Petrovsky")
                                .email("petrikin1996@gmail.com")
                        )
                ).components(new Components()
                        .addSecuritySchemes("keycloak", createOauth2SecurityScheme())
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("keycloak")
                );
    }

    private SecurityScheme createOauth2SecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl(
                                        "http://localhost:8080/realms/my-bank-realm/protocol/openid-connect/auth"
                                )
                                .tokenUrl(
                                        "http://localhost:8080/realms/my-bank-realm/protocol/openid-connect/token"
                                )
                                .scopes(new Scopes()
                                        .addString("openid", "openid")
                                        .addString("email", "email")
                                        .addString("profile", "profile")
                                )
                        ));
    }
}
