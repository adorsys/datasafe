package de.adorsys.datasafe.encrypiton.impl.keystore;

import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.exceptions.KeyStoreAuthException;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyStoreAuthTest extends BaseMockitoTest {

    @Test
    void noPasswords() {
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(null, null);
        assertThrows(KeyStoreAuthException.class, () -> keyStoreAuth.getReadKeyPassword());
        assertThrows(KeyStoreAuthException.class, () -> keyStoreAuth.getReadKeyPassword());
    }

    @Test
    void testToString() {
        String s = new KeyStoreAuth(null, null).toString();
        assertThat(s.contains("null")).isTrue();
    }
}
