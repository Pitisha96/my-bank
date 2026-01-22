package com.pitisha.project.mybank.accountservice.config;

import static java.util.stream.Stream.concat;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private static final String PREFERRED_USERNAME = "preferred_username";
    private static final String SPRING_SEC_ROLES = "spring_sec_roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/internal**").hasAuthority("SCOPE_accounts")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        final var converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName(PREFERRED_USERNAME);
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
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
}
