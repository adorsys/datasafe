package de.adorsys.datasafe.business.impl.encryption.pathencryption.encryption;

import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import org.junit.jupiter.api.Test;

public class KeyPairGeneratorTest {
    @Test
    public void a() {
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("read");
        ExtendedKeyPairGeneratorImpl i = new ExtendedKeyPairGeneratorImpl("RSA", 2048, "SHA256withRSA", "enc");
        i.setDayAfter(40);
        i.setWithCA(true);
        i.generateEncryptionKey("affe", readKeyPassword);
    }
}
