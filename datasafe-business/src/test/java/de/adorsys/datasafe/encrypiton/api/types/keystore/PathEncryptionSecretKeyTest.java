package de.adorsys.datasafe.encrypiton.api.types.keystore;

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

    @Test
    void validateAllOk() {
        PathEncryptionSecretKey key = new PathEncryptionSecretKey(
                secretKeyId, secretKey, counterKeyId, counterSecretKey
        );

        assertThat(key.getSecretKey()).isEqualTo(secretKey);
        assertThat(key.getCounterSecretKey()).isEqualTo(counterSecretKey);
    }

    @Test
    void validateNullSecretKeyIdImpossible() {
        assertThrows(NullPointerException.class, () ->
                new PathEncryptionSecretKey(null, secretKey, counterKeyId, counterSecretKey)
        );
    }

    @Test
    void validateNullSecretKeyImpossible() {
        assertThrows(NullPointerException.class, () ->
                new PathEncryptionSecretKey(secretKeyId, null, counterKeyId, counterSecretKey)
        );
    }

    @Test
    void validateNullCounterKeyIdImpossible() {
        assertThrows(NullPointerException.class, () ->
                new PathEncryptionSecretKey(secretKeyId, secretKey, null, counterSecretKey)
        );
    }

    @Test
    void validateNullCounterKeyImpossible() {
        assertThrows(NullPointerException.class, () ->
                new PathEncryptionSecretKey(secretKeyId, secretKey, counterKeyId, null)
        );
    }
}
