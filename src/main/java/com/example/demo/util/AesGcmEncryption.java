package com.example.demo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** Encrypts stored third-party secrets with authenticated AES-GCM; the key must live outside the database. */
// AES-GCM (Full Form - Authenticated Encryption with Galois/Counter Mode) is a modern authenticated encryption mode that prevents both eavesdropping and tampering. It is widely supported and fast in hardware. The key must be 32 bytes (256 bits) for AES-256, which is the strongest AES variant. The nonce (IV) must be unique per encryption; it is stored alongside the ciphertext because it is not secret. The authentication tag is appended to the ciphertext by the Cipher class, so it does not need to be stored separately.
@Component
public class AesGcmEncryption {

    private static final int NONCE_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmEncryption(@Value("${app.crypto.aes-key-base64}") String keyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("AES-GCM key must be exactly 32 decoded bytes (AES-256)");
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        try {
            byte[] nonce = new byte[NONCE_LENGTH_BYTES];
            secureRandom.nextBytes(nonce); // A fresh nonce prevents repeated values encrypting identically.
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] packed = new byte[nonce.length + ciphertext.length];
            System.arraycopy(nonce, 0, packed, 0, nonce.length); // Store nonce beside ciphertext; neither is secret.
            System.arraycopy(ciphertext, 0, packed, nonce.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(packed);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not encrypt credential", exception);
        }
    }

    public String decrypt(String encryptedValue) {
        try {
            byte[] packed = Base64.getDecoder().decode(encryptedValue);
            if (packed.length <= NONCE_LENGTH_BYTES) {
                throw new IllegalArgumentException("Encrypted value is too short");
            }
            byte[] nonce = Arrays.copyOfRange(packed, 0, NONCE_LENGTH_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(packed, NONCE_LENGTH_BYTES, packed.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            return new String(cipher.doFinal(ciphertext), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not decrypt credential; key or encrypted data may be wrong", exception);
        }
    }
}