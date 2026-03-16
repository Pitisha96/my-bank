package com.pitisha.project.mybank.accountservice.domain.entity.converter;

import com.pitisha.project.mybank.accountservice.domain.service.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class AccountNumberConverter implements AttributeConverter<String, String> {

    private final EncryptionService encryptionService;

    @Override
    public String convertToDatabaseColumn(final String plainText) {
        return encryptionService.encrypt(plainText);
    }

    @Override
    public String convertToEntityAttribute(final String encryptedText) {
        return encryptionService.decrypt(encryptedText);
    }
}
