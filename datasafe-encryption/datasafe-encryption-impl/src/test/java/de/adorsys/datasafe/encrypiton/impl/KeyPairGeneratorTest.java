package de.adorsys.datasafe.encrypiton.impl;

import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class KeyPairGeneratorTest extends WithBouncyCastle {

    @Test
    void testKeyPairGenerationWithCA() {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordTestFactory.getForString("read");
        TestableKeyPairGeneratorImpl i = new TestableKeyPairGeneratorImpl("RSA", 2048, "SHA256withRSA", "enc");
        i.setDayAfter(40);
        i.setWithCA(true);

        assertThat(
                i.generateEncryptionKey("affe", readKeyPassword)
                        .getKeyPair()
                        .getSubjectCert()
                        .isValidOn(new Date())
        ).isTrue();
    }
}
