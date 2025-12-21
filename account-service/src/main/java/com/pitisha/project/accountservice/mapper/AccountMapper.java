package com.pitisha.project.accountservice.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.pitisha.project.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.accountservice.domain.entity.AccountEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface AccountMapper {
    AccountResponse entityToDto(AccountEntity accountEntity);
}
