package com.pitisha.project.mybank.transactionservice.domain.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.pitisha.project.mybank.transactionservice.api.dto.response.TransactionResponse;
import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface TransactionMapper {

    @Mapping(
            target = "amount",
            expression = "java(source.getAmount() != null ? source.getAmount().setScale(2, java.math.RoundingMode.HALF_UP) : null)"
    )
    TransactionResponse toDto(TransactionEntity source);
}
