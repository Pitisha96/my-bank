package com.pitisha.project.mybank.accountservice.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.util.Map;

import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.fill;
import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;
import static java.util.Objects.isNull;

@Service
public class EncryptionServiceImpl implements EncryptionService {

    private static final String ENCRYPTION_KEY_PATH = "secret/data/account-service/account-number-encryption-key";
    private static final String HMAC_KEY_PATH = "secret/data/account-service/account-number-hash-key";
    private static final String ENCRYPTION_KEY = "encryption_key";
    private static final String HMAC_KEY = "hmac_key";
    private static final String AES = "AES";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String VAULT_ENCRYPTION_KEY_NOT_FOUND = "Vault encryption key not found";
    private static final String DATA = "data";
    private static final String BYTE_FORMAT = "%02x";
    private static final String ENCRYPTION_EXCEPTION = "Failed to encrypt data";
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private final SecretKeySpec aesKey;
    private final SecretKeySpec hmacKey;

    public EncryptionServiceImpl(final VaultOperations vaultOperations) {
        byte[] aesKeyBytes = getKeyFromVault(vaultOperations, ENCRYPTION_KEY_PATH, ENCRYPTION_KEY);
        byte[] hmacKeyBytes = getKeyFromVault(vaultOperations, HMAC_KEY_PATH, HMAC_KEY);
        try {
            this.aesKey = new SecretKeySpec(aesKeyBytes, AES);
            this.hmacKey = new SecretKeySpec(hmacKeyBytes, HMAC_SHA256);
        } finally {
            fill(aesKeyBytes, (byte) 0);
            fill(hmacKeyBytes, (byte) 0);
        }
    }

    @SuppressWarnings("unchecked")
    private static byte[] getKeyFromVault(final VaultOperations vaultOperations, final String path, final String key) {
        final VaultResponse response = vaultOperations.read(path);
        if (isNull(response) || isNull(response.getData())) {
            throw new IllegalStateException(VAULT_ENCRYPTION_KEY_NOT_FOUND);
        }
        final Map<String, Object> data = (Map<String, Object>) response.getData().get(DATA);
        return getDecoder().decode((String) data.get(key));
    }

    @Override
    public String encrypt(final String plainText) {
        try {
            final byte[] iv = generateIv();
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
            final byte[] encrypted = cipher.doFinal(plainText.getBytes(UTF_8));
            return getEncoder().encodeToString(concat(iv, encrypted));
        } catch (Exception e) {
            throw new IllegalStateException(ENCRYPTION_EXCEPTION, e);
        }
    }

    @Override
    public String decrypt(final String encryptedText) {
        try {
            final byte[] data = getDecoder().decode(encryptedText);
            byte[] iv = copyOfRange(data, 0, 12);
            byte[] cipherText = copyOfRange(data, 12, data.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(cipherText), UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(ENCRYPTION_EXCEPTION, e);
        }
    }

    @Override
    public String generateHmac(final String plainText) {
        try {
            final Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(hmacKey);
            return bytesToHex(mac.doFinal(plainText.getBytes(UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(ENCRYPTION_EXCEPTION, e);
        }
    }

    private byte[] generateIv() {
        final byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private byte[] concat(final byte[] iv, final byte[] encrypted) {
        final byte[] result = new byte[iv.length + encrypted.length];
        arraycopy(iv, 0, result, 0, iv.length);
        arraycopy(encrypted, 0, result, iv.length, encrypted.length);
        return result;
    }

    private String bytesToHex(final byte[] bytes) {
        final StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (final byte b : bytes) {
            builder.append(BYTE_FORMAT.formatted(b));
        }
        return builder.toString();
    }
}
