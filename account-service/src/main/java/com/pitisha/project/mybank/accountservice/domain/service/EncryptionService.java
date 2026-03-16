package com.pitisha.project.mybank.accountservice.domain.service;

public interface EncryptionService {

    String encrypt(String plainText);

    String decrypt(String encryptedText);

    String generateHmac(String plainText);
}
