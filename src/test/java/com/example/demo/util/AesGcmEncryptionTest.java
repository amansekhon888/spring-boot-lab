package com.example.demo.util;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Confirms credential encryption round-trips and GCM rejects tampered ciphertext. */
class AesGcmEncryptionTest {

    private static final String TEST_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void encryptedCredentialRoundTrips() {
        AesGcmEncryption encryption = new AesGcmEncryption(TEST_KEY);

        assertEquals("provider-secret", encryption.decrypt(encryption.encrypt("provider-secret")));
    }

    @Test
    void modifiedCiphertextIsRejected() {
        AesGcmEncryption encryption = new AesGcmEncryption(TEST_KEY);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryption.encrypt("provider-secret"));
        encryptedBytes[encryptedBytes.length - 1] ^= 1;
        String tamperedValue = Base64.getEncoder().encodeToString(encryptedBytes);

        assertThrows(IllegalStateException.class, () -> encryption.decrypt(tamperedValue));
    }
}