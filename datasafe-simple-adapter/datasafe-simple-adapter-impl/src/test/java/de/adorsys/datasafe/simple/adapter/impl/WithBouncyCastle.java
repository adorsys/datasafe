package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;

import java.security.Security;

public abstract class WithBouncyCastle extends BaseMockitoTest {

    @BeforeAll
    static void setupBouncyCastle() {
        Security.addProvider(new BouncyCastleProvider());
    }
}
