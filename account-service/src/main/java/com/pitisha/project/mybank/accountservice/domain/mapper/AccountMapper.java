package com.pitisha.project.mybank.accountservice.domain.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface AccountMapper {

    @Mapping(
            target = "balance",
            expression = "java(source.getBalance() != null ? source.getBalance().setScale(2, java.math.RoundingMode.HALF_UP) : null)"
    )
    AccountResponse entityToDto(AccountEntity source);
}
