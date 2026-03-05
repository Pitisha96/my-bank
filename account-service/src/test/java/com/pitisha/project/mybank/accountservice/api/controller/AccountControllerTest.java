package com.pitisha.project.mybank.accountservice.api.controller;

import com.jayway.jsonpath.JsonPath;
import com.pitisha.project.mybank.accountservice.api.dto.request.AccountFilter;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountPageResponse;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.mybank.accountservice.api.security.RestAccessDeniedHandler;
import com.pitisha.project.mybank.accountservice.api.security.RestAuthenticationEntryPoint;
import com.pitisha.project.mybank.accountservice.config.WebSecurityConfig;
import com.pitisha.project.mybank.accountservice.domain.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus.ACTIVE;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus.BLOCKED;
import static com.pitisha.project.mybank.domain.entity.AccountCurrency.BYN;
import static com.pitisha.project.mybank.domain.entity.AccountCurrency.RUB;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = AccountController.class,
    properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9999"
)
@Import({WebSecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
public class AccountControllerTest {

    private static final String REGEX_FORMAT = "^%s(:00)?$";
    private static final UUID OWNER_ID = fromString("e295fcb3-f60b-474f-b097-58a0d6123795");
    private static final UUID ACC_1_NUMBER = fromString("a7ca089d-72fb-4319-9c8b-8c71e642acc4");
    private static final UUID ACC_2_NUMBER = fromString("0acab90c-2ec7-49be-a4c6-6b7ccb8e8f51");
    private static final LocalDateTime ACC_1_CREATED_AT = LocalDateTime.parse("2026-03-03T00:00:30");
    private static final LocalDateTime ACC_2_CREATED_AT = LocalDateTime.parse("2026-03-03T23:59:59");

    private final AccountResponse acc1 = new AccountResponse(
        ACC_1_NUMBER,
        OWNER_ID,
        BYN,
        new BigDecimal("100.25"),
        ACTIVE,
        ACC_1_CREATED_AT
    );

    private final AccountResponse acc2 = new AccountResponse(
        ACC_2_NUMBER,
        OWNER_ID,
        RUB,
        new BigDecimal("999.99"),
        BLOCKED,
        ACC_2_CREATED_AT
    );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    private static Stream<Arguments> getCurrentLocalDatePlusDay() {
        return Stream.of(
            Arguments.of(LocalDate.now().plusDays(1).toString())
        );
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    @DisplayName("[Find accounts] When an user with the role admin searches for accounts then return the account page response")
    public void when_admin_user_searches_for_accounts_then_return_account_page_response() throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));
        mockMvc.perform(get("/api/v1/accounts")
                .param("page", "0")
                .param("pageSize", "50")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].number").value(ACC_1_NUMBER.toString()))
            .andExpect(jsonPath("$.data[0].ownerId").value(OWNER_ID.toString()))
            .andExpect(jsonPath("$.data[0].currency").value(BYN.name()))
            .andExpect(jsonPath("$.data[0].balance").value(100.25))
            .andExpect(jsonPath("$.data[0].status").value(ACTIVE.name()))
            .andExpect(jsonPath("$.data[0].createdAt").value(matchesPattern(REGEX_FORMAT.formatted(ACC_1_CREATED_AT.toString()))))
            .andExpect(jsonPath("$.data[1].number").value(ACC_2_NUMBER.toString()))
            .andExpect(jsonPath("$.data[1].ownerId").value(OWNER_ID.toString()))
            .andExpect(jsonPath("$.data[1].currency").value(RUB.name()))
            .andExpect(jsonPath("$.data[1].balance").value(999.99))
            .andExpect(jsonPath("$.data[1].status").value(BLOCKED.name()))
            .andExpect(jsonPath("$.data[1].createdAt").value(matchesPattern(REGEX_FORMAT.formatted(ACC_2_CREATED_AT.toString()))))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.currentPage").value(0))
            .andExpect(jsonPath("$.pageSize").value(50));

        verify(accountService, times(1)).findAll(any(AccountFilter.class));
    }

    @Test
    @DisplayName("[Find accounts] When an user with the role user searches for self accounts then return the account page response")
    public void when_user_searches_for_self_accounts_then_return_account_page_response() throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        mockMvc.perform(get("/api/v1/accounts")
                .with(jwt().jwt(builder -> builder
                        .claim("sub", OWNER_ID.toString())
                        .claim("spring_sec_roles", "ROLE_USER")
                    )
                )
                .param("ownerId", OWNER_ID.toString())
                .param("page", "0")
                .param("pageSize", "50")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].number").value(ACC_1_NUMBER.toString()))
            .andExpect(jsonPath("$.data[0].ownerId").value(OWNER_ID.toString()))
            .andExpect(jsonPath("$.data[0].currency").value(BYN.name()))
            .andExpect(jsonPath("$.data[0].balance").value(100.25))
            .andExpect(jsonPath("$.data[0].status").value(ACTIVE.name()))
            .andExpect(jsonPath("$.data[0].createdAt").value(matchesPattern(REGEX_FORMAT.formatted(ACC_1_CREATED_AT.toString()))))
            .andExpect(jsonPath("$.data[1].number").value(ACC_2_NUMBER.toString()))
            .andExpect(jsonPath("$.data[1].ownerId").value(OWNER_ID.toString()))
            .andExpect(jsonPath("$.data[1].currency").value(RUB.name()))
            .andExpect(jsonPath("$.data[1].balance").value(999.99))
            .andExpect(jsonPath("$.data[1].status").value(BLOCKED.name()))
            .andExpect(jsonPath("$.data[1].createdAt").value(matchesPattern(REGEX_FORMAT.formatted(ACC_2_CREATED_AT.toString()))))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.currentPage").value(0))
            .andExpect(jsonPath("$.pageSize").value(50));

        verify(accountService, times(1)).findAll(any(AccountFilter.class));
    }

    @Test
    @DisplayName("[Find accounts] When an anonymous user searches for accounts then return the unauthorized error response")
    public void when_anonymous_user_searches_for_accounts_then_return_unauthorized() throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        final String content = mockMvc.perform(get("/api/v1/accounts"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.message").value("Unauthorized"))
            .andExpect(jsonPath("$.details").value(nullValue()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, never()).findAll(any(AccountFilter.class));
    }

    @Test
    @DisplayName("[Find accounts] When an user with the role user searches for other users' accounts then return forbidden error response")
    public void when_user_searches_for_other_users_accounts_then_return_forbidden() throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        final String content = mockMvc.perform(get("/api/v1/accounts")
            .with(jwt().jwt(builder -> builder
                    .claim("sub", OWNER_ID.toString())
                    .claim("spring_sec_roles", "ROLE_USER")
                )
            )
            .param("ownerId", "18d22d43-d73c-451d-b5ba-073006adb9a4")
            .param("page", "0")
            .param("pageSize", "50"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"))
            .andExpect(jsonPath("$.message").value("Forbidden"))
            .andExpect(jsonPath("$.details").value(nullValue()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, never()).findAll(any(AccountFilter.class));
    }

    @WithMockUser(roles = "ADMIN")
    @ParameterizedTest
    @NullSource
    @CsvSource({
        "-1",
        "test"
    })
    @DisplayName("[Find accounts] When page query param is invalid then return validation error response")
    public void when_page_query_param_is_invalid_then_return_validation_error(final String page) throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        final MockHttpServletRequestBuilder req = get("/api/v1/accounts")
            .param("pageSize", "50");
        if (nonNull(page)) {
            req.param("page", page);
        }
        final String content = mockMvc.perform(req)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Validation error"))
            .andExpect(jsonPath("$.details").value(aMapWithSize(1)))
            .andExpect(jsonPath("$.details").value(hasKey("page")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, never()).findAll(any(AccountFilter.class));
    }

    @WithMockUser(roles = "ADMIN")
    @ParameterizedTest
    @NullSource
    @CsvSource({
        "0",
        "51",
        "test"
    })
    @DisplayName("[Find accounts] When pageSize query param is invalid then return validation error response")
    public void when_pageSize_query_param_is_invalid_then_return_validation_error(final String pageSize) throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        final MockHttpServletRequestBuilder req = get("/api/v1/accounts")
            .param("page", "0");
        if (nonNull(pageSize)) {
            req.param("pageSize", pageSize);
        }
        final String content = mockMvc.perform(req)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Validation error"))
            .andExpect(jsonPath("$.details").value(aMapWithSize(1)))
            .andExpect(jsonPath("$.details").value(hasKey("pageSize")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, never()).findAll(any(AccountFilter.class));
    }

    @WithMockUser(roles = "ADMIN")
    @ParameterizedTest
    @CsvSource({
        "-1",
        "test",
        "1111111111111111111.11",
        "11.123"
    })
    @DisplayName("[Find accounts] When balanceFrom query param is invalid then return validation error response")
    public void when_balanceFrom_query_param_is_invalid_then_return_validation_error(final String balance) throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        final String content = mockMvc.perform(get("/api/v1/accounts")
                .param("balanceFrom", balance)
                .param("page", "0")
                .param("pageSize", "50")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Validation error"))
            .andExpect(jsonPath("$.details").value(aMapWithSize(1)))
            .andExpect(jsonPath("$.details").value(hasKey("balanceFrom")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, never()).findAll(any(AccountFilter.class));
    }

    @WithMockUser(roles = "ADMIN")
    @ParameterizedTest
    @CsvSource({
        "-1",
        "test",
        "1111111111111111111.11",
        "11.123"
    })
    @DisplayName("[Find accounts] When balanceTo query param is invalid then return validation error response")
    public void when_balanceTo_query_param_is_invalid_then_return_validation_error(final String balance) throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        final String content = mockMvc.perform(get("/api/v1/accounts")
                .param("balanceTo", balance)
                .param("page", "0")
                .param("pageSize", "50")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Validation error"))
            .andExpect(jsonPath("$.details").value(aMapWithSize(1)))
            .andExpect(jsonPath("$.details").value(hasKey("balanceTo")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, never()).findAll(any(AccountFilter.class));
    }

    @WithMockUser(roles = "ADMIN")
    @ParameterizedTest
    @MethodSource("getCurrentLocalDatePlusDay")
    @CsvSource({
        "test"
    })
    @DisplayName("[Find accounts] When createdFrom query param is invalid then return validation error response")
    public void when_createdFrom_query_param_is_invalid_then_return_validation_error(final String date) throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        final String content = mockMvc.perform(get("/api/v1/accounts")
                .param("createdFrom", date)
                .param("page", "0")
                .param("pageSize", "50")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Validation error"))
            .andExpect(jsonPath("$.details").value(aMapWithSize(1)))
            .andExpect(jsonPath("$.details").value(hasKey("createdFrom")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, never()).findAll(any(AccountFilter.class));
    }

    @WithMockUser(roles = "ADMIN")
    @ParameterizedTest
    @MethodSource("getCurrentLocalDatePlusDay")
    @CsvSource({
        "test"
    })
    @DisplayName("[Find accounts] When createdTo query param is invalid then return validation error response")
    public void when_createdTo_query_param_is_invalid_then_return_validation_error(final String date) throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        final String content = mockMvc.perform(get("/api/v1/accounts")
                .param("createdTo", date)
                .param("page", "0")
                .param("pageSize", "50")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Validation error"))
            .andExpect(jsonPath("$.details").value(aMapWithSize(1)))
            .andExpect(jsonPath("$.details").value(hasKey("createdTo")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, never()).findAll(any(AccountFilter.class));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    @DisplayName("[Find accounts] When ownerId query param type mismatch then return validation error response")
    public void when_ownerId_query_param_type_mismatch_then_return_validation_error() throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        final String content = mockMvc.perform(get("/api/v1/accounts")
                .param("ownerId", "test")
                .param("page", "0")
                .param("pageSize", "50")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Validation error"))
            .andExpect(jsonPath("$.details").value(aMapWithSize(1)))
            .andExpect(jsonPath("$.details").value(hasKey("ownerId")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, never()).findAll(any(AccountFilter.class));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    @DisplayName("[Find accounts] When currency query param type mismatch then return validation error response")
    public void when_currency_query_param_type_mismatch_then_return_validation_error() throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        final String content = mockMvc.perform(get("/api/v1/accounts")
                .param("currency", "test")
                .param("page", "0")
                .param("pageSize", "50")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Validation error"))
            .andExpect(jsonPath("$.details").value(aMapWithSize(1)))
            .andExpect(jsonPath("$.details").value(hasKey("currency")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, never()).findAll(any(AccountFilter.class));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    @DisplayName("[Find accounts] When status query param type mismatch then return validation error response")
    public void when_status_query_param_type_mismatch_then_return_validation_error() throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenReturn(new AccountPageResponse(List.of(acc1, acc2), 1, 0, 50));

        final String content = mockMvc.perform(get("/api/v1/accounts")
                .param("status", "test")
                .param("page", "0")
                .param("pageSize", "50")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Validation error"))
            .andExpect(jsonPath("$.details").value(aMapWithSize(1)))
            .andExpect(jsonPath("$.details").value(hasKey("status")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, never()).findAll(any(AccountFilter.class));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    @DisplayName("[Find accounts] When thrown exception then return internal server error response")
    public void when_thrown_exception_then_return_internal_server_error_response() throws Exception {
        when(accountService.findAll(any(AccountFilter.class))).thenThrow(new RuntimeException("Test error"));

        final String content = mockMvc.perform(get("/api/v1/accounts")
                .param("page", "0")
                .param("pageSize", "50")
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
            .andExpect(jsonPath("$.message").value("Internal Server Error"))
            .andExpect(jsonPath("$.details").value(nullValue()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final LocalDateTime timestamp = LocalDateTime.parse(JsonPath.parse(content).read("$.timestamp", String.class));
        assertThat(timestamp).isCloseTo(now(), within(5, SECONDS));

        verify(accountService, times(1)).findAll(any(AccountFilter.class));
    }
}
