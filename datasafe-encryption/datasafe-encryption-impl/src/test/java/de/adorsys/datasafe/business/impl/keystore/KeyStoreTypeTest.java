package de.adorsys.datasafe.business.impl.keystore;

import de.adorsys.datasafe.business.api.types.keystore.KeyStoreType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class KeyStoreTypeTest {

    @Test
    public void typeBySystemEnv() {
        String value = UUID.randomUUID().toString();
        System.setProperty("SERVER_KEYSTORE_TYPE", value);
        Assertions.assertEquals(value, ExtendedKeyStoreType.getStaticDefaultKeyStoreType().getValue());
        System.getProperties().remove("SERVER_KEYSTORE_TYPE");
    }

    public static class ExtendedKeyStoreType extends KeyStoreType {
        public ExtendedKeyStoreType(String value) {
            super(value);
        }

        public static KeyStoreType getStaticDefaultKeyStoreType() {
            return KeyStoreType.getDefaultKeyStoreType();
        }
    }
}
