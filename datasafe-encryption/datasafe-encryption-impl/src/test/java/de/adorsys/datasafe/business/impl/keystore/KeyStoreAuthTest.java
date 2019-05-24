package de.adorsys.datasafe.business.impl.keystore;

import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.types.keystore.exceptions.KeyStoreAuthException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KeyStoreAuthTest {
    @Test
    public void noPasswords() {
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(null, null);
        assertThrows(KeyStoreAuthException.class, () -> keyStoreAuth.getReadKeyPassword());
        assertThrows(KeyStoreAuthException.class, () -> keyStoreAuth.getReadKeyPassword());
    }

    @Test
    public void testToString() {
        String s = new KeyStoreAuth(null, null).toString();
        assertThat(s.contains("null")).isTrue();
    }
}
