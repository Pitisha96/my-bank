package com.pitisha.project.mybank.accountservice.domain.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface AccountMapper {
    AccountResponse entityToDto(AccountEntity accountEntity);
}
