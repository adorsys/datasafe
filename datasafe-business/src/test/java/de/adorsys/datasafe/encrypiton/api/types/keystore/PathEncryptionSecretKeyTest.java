package de.adorsys.datasafe.encrypiton.api.types.keystore;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PathEncryptionSecretKeyTest extends BaseMockitoTest {

    @Mock
    private KeyID secretKeyId;

    @Mock
    private SecretKey secretKey;

    @Mock
    private KeyID counterKeyId;

    @Mock
    private SecretKey counterSecretKey;

    private UserID user = new UserID("user");

    @Test
    void validateAllOk() {
        AuthPathEncryptionSecretKey key = new AuthPathEncryptionSecretKey(
                new UserID("user"),
                new SecretKeyIDWithKey(secretKeyId, secretKey),
                new SecretKeyIDWithKey(counterKeyId, counterSecretKey)
        );

        assertThat(key.getSecretKey().getSecretKey()).isEqualTo(secretKey);
        assertThat(key.getCounterSecretKey().getSecretKey()).isEqualTo(counterSecretKey);
    }

    @Test
    void validateNullUserImpossible() {
        assertThrows(NullPointerException.class, () ->
                new AuthPathEncryptionSecretKey(
                        null,
                        new SecretKeyIDWithKey(secretKeyId, secretKey),
                        new SecretKeyIDWithKey(counterKeyId, counterSecretKey))
        );
    }

    @Test
    void validateNullSecretKeyImpossible() {
        assertThrows(NullPointerException.class, () ->
                new AuthPathEncryptionSecretKey(
                        user,
                        null,
                        new SecretKeyIDWithKey(counterKeyId, counterSecretKey))
        );
    }

    @Test
    void validateNullCounterKeyImpossible() {
        assertThrows(NullPointerException.class, () ->
                new AuthPathEncryptionSecretKey(
                        user,
                        new SecretKeyIDWithKey(secretKeyId, secretKey),
                        null)
        );
    }
}
