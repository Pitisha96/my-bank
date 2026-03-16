package com.pitisha.project.mybank.accountservice.domain.mapper;

import static java.math.RoundingMode.HALF_UP;
import static java.util.Objects.isNull;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = SPRING)
public interface AccountMapper {

    @Mapping(
            target = "balance",
            expression = "java(mapBalance(source.getBalance(), source.getReserved()))"
    )
    AccountResponse entityToDto(AccountEntity source);

    @SuppressWarnings("unused")
    default BigDecimal mapBalance(final BigDecimal balance, final BigDecimal reserved) {
        if (isNull(balance)) {
            return null;
        }
        if (isNull(reserved)) {
            return balance.setScale(2, HALF_UP);
        }
        return balance.subtract(reserved).setScale(2, HALF_UP);
    }
}
