package de.adorsys.datasafe.encrypiton.impl;

import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import org.junit.jupiter.api.Test;

public class KeyPairGeneratorTest {
    @Test
    public void testKeyPairGenerationWithCA() {
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("read");
        ExtendedKeyPairGeneratorImpl i = new ExtendedKeyPairGeneratorImpl("RSA", 2048, "SHA256withRSA", "enc");
        i.setDayAfter(40);
        i.setWithCA(true);
        i.generateEncryptionKey("affe", readKeyPassword);
    }
}
