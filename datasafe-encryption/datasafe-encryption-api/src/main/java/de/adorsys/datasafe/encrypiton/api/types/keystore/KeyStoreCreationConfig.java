package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Wrapper that contains count of public-key pairs and count of encryption keys.
 */
@Getter
@RequiredArgsConstructor
public class KeyStoreCreationConfig {

    public static final KeyID PATH_KEY_ID = new KeyID("PATH_SECRET");
    public static final KeyID SYMM_KEY_ID = new KeyID("PRIVATE_SECRET");

    private final int encKeyNumber;
    private final int signKeyNumber;
}
