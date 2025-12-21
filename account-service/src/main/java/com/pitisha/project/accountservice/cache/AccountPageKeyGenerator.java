package com.pitisha.project.accountservice.cache;

import com.pitisha.project.accountservice.api.dto.request.AccountFilter;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("accountPageFilterKeyGenerator")
public class AccountPageKeyGenerator implements KeyGenerator {

    private static final String ILLEGAL_ARGUMENT_ERROR_MESSAGE = "Method must have AccountFilter as the first argument";
    private static final String KEY_FORMAT = "%s:%s:%s:%s:%s:%s:%s:%s:%s";

    @Override
    public Object generate(final Object target, final Method method, final @Nullable Object... params) {
        if (params.length == 0 || !(params[0] instanceof AccountFilter accountFilter)) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_ERROR_MESSAGE);
        }
        return KEY_FORMAT.formatted(
                accountFilter.ownerId(),
                accountFilter.currency(),
                accountFilter.balanceFrom(),
                accountFilter.balanceTo(),
                accountFilter.status(),
                accountFilter.createdFrom(),
                accountFilter.createdTo(),
                accountFilter.page(),
                accountFilter.pageSize()
        );
    }
}
