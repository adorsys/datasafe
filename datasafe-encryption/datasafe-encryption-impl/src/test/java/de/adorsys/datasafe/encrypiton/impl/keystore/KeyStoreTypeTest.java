package de.adorsys.datasafe.encrypiton.impl.keystore;

import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreType;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class KeyStoreTypeTest extends BaseMockitoTest {

    @Test
    void typeBySystemEnv() {
        String value = this.getClass().getSimpleName() + UUID.randomUUID().toString();
        System.setProperty("SERVER_KEYSTORE_TYPE", value);
        Assertions.assertEquals(value, TestableKeyStoreType.getStaticDefaultKeyStoreType().getValue());
        System.clearProperty("SERVER_KEYSTORE_TYPE");
        TestableKeyStoreType.resetType();
    }

    public static class TestableKeyStoreType extends KeyStoreType {

        public TestableKeyStoreType(String value) {
            super(value);
        }

        static KeyStoreType getStaticDefaultKeyStoreType() {
            return KeyStoreType.getDefaultKeyStoreType();
        }

        static void resetType() {
            DEFAULT = getStaticDefaultKeyStoreType();
        }
    }
}
