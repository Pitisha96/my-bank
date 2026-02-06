package com.pitisha.project.mybank.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;
import static org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain webSecurityFilterChain(final HttpSecurity http, final ClientRegistrationRepository clients) {
        http
            .securityMatcher(request ->
                !request.getRequestURI().startsWith("/api") && !request.getRequestURI().startsWith("/ws")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login/**",
                    "/logout/**",
                    "/logged-out/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/error"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/keycloak")
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                    .oidcUserService(this.keycloakOAuth2UserService()))
            )
            .logout(logout -> logout
                .logoutSuccessHandler(oidcLogoutSuccessHandler(clients))
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(withHttpOnlyFalse())
            );
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) {
        http
            .securityMatcher("/api/**", "/ws/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(withHttpOnlyFalse())
                .ignoringRequestMatchers("/ws/**")
            )
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    OAuth2UserService<OidcUserRequest, OidcUser> keycloakOAuth2UserService() {
        final var oidcUserService = new OidcUserService();
        return userRequest -> {
            final OidcUser oidcUser = oidcUserService.loadUser(userRequest);
            final Set<GrantedAuthority> authorities = extractSpringSecurityRoles(oidcUser);
            authorities.addAll(oidcUser.getAuthorities());
            final String userNameAttributeName = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), userNameAttributeName);
        };
    }

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler(final ClientRegistrationRepository repository) {
        final var handler = new OidcClientInitiatedLogoutSuccessHandler(repository);
        handler.setPostLogoutRedirectUri("http://localhost:8083/logged-out");
        return handler;
    }

    private Set<GrantedAuthority> extractSpringSecurityRoles(final OidcUser oidcUser) {
        final List<String> roles = oidcUser.getClaim("spring_sec_roles");
        if (isNull(roles)) {
            return emptySet();
        }
        return roles.stream()
            .filter(role -> role.startsWith("ROLE_"))
            .map(SimpleGrantedAuthority::new)
            .map(GrantedAuthority.class::cast)
            .collect(toSet());
    }
}
