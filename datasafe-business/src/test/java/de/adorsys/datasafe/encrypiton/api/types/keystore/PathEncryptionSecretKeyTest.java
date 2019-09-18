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
                new SecretKeyIDWithKey(secretKeyId, secretKey), new SecretKeyIDWithKey(counterKeyId, counterSecretKey)
        );

        assertThat(key.getSecretKey().getSecretKey()).isEqualTo(secretKey);
        assertThat(key.getCounterSecretKey().getSecretKey()).isEqualTo(counterSecretKey);
    }

    @Test
    void validateNullSecretKeyImpossible() {
        assertThrows(NullPointerException.class, () ->
                new PathEncryptionSecretKey(null, new SecretKeyIDWithKey(counterKeyId, counterSecretKey))
        );
    }

    @Test
    void validateNullCounterKeyImpossible() {
        assertThrows(NullPointerException.class, () ->
                new PathEncryptionSecretKey(new SecretKeyIDWithKey(secretKeyId, secretKey), null)
        );
    }
}
