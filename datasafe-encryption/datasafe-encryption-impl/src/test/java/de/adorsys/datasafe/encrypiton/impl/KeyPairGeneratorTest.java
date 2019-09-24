package de.adorsys.datasafe.encrypiton.impl;

import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.Security;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class KeyPairGeneratorTest extends BaseMockitoTest {

    @BeforeAll
    static void setupBouncyCastle() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void testKeyPairGenerationWithCA() {
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("read");
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
