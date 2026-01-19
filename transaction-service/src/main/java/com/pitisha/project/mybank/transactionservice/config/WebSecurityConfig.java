package com.pitisha.project.mybank.transactionservice.config;

import static java.util.stream.Stream.concat;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.pitisha.project.mybank.transactionservice.api.security.RestAccessDeniedHandler;
import com.pitisha.project.mybank.transactionservice.api.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final String PREFERRED_USERNAME = "preferred_username";
    private static final String SPRING_SEC_ROLES = "spring_sec_roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http,
                                                   final RestAuthenticationEntryPoint authenticationEntryPoint,
                                                   final RestAccessDeniedHandler accessDeniedHandler) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .exceptionHandling(exceptionHandler -> exceptionHandler
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        final var converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName(PREFERRED_USERNAME);
        converter.setJwtGrantedAuthoritiesConverter( jwt -> {
            final var authorities = new JwtGrantedAuthoritiesConverter().convert(jwt);
            final var roles = jwt.getClaimAsStringList(SPRING_SEC_ROLES).stream()
                    .filter(role -> role.startsWith(ROLE_PREFIX))
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast);
            return concat(authorities.stream(), roles)
                    .toList();
        });
        return converter;
    }

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(final ClientRegistrationRepository registrations,
                                                          final OAuth2AuthorizedClientService clientService) {
        final OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        final AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clientService);

        manager.setAuthorizedClientProvider(provider);
        return manager;
    }
}
