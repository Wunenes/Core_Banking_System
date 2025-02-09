package com.bankingSystem.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Base64;

@Converter
public class AttributeEncryptor implements AttributeConverter<String, String> {
    private static final String KEY = "1234567890abcdef";

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedData = cipher.doFinal(attribute.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }
    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedData = Base64.getDecoder().decode(dbData);
            return new String(cipher.doFinal(decodedData));
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}
