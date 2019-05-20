package de.adorsys.datasafe.business.impl.keystore;

import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.types.keystore.exceptions.KeyStoreAuthException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KeyStoreAuthTest {
    @Test
    public void noPasswords() {
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(null, null);
        Assertions.assertThrows(KeyStoreAuthException.class, () -> keyStoreAuth.getReadKeyPassword());
        Assertions.assertThrows(KeyStoreAuthException.class, () -> keyStoreAuth.getReadKeyPassword());
    }

    @Test
    public void testToString() {
        String s = new KeyStoreAuth(null, null).toString();
    }
}
