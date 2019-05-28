package de.adorsys.datasafe.encrypiton.impl;

import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import org.junit.jupiter.api.Test;

class KeyPairGeneratorTest {

    @Test
    void testKeyPairGenerationWithCA() {
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("read");
        TestableKeyPairGeneratorImpl i = new TestableKeyPairGeneratorImpl("RSA", 2048, "SHA256withRSA", "enc");
        i.setDayAfter(40);
        i.setWithCA(true);
        i.generateEncryptionKey("affe", readKeyPassword);
    }
}
