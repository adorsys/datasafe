package de.adorsys.datasafe.business.api.version.types.keystore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by peter on 26.02.18 at 17:04.
 */
@Getter
@RequiredArgsConstructor
public class KeyStoreCreationConfig {

    public static final KeyID PATH_KEY_ID = new KeyID("PATH_SECRET");
    public static final KeyID SYMM_KEY_ID = new KeyID("PRIVATE_SECRET");

    private final int encKeyNumber;
    private final int signKeyNumber;
    private final int secretKeyNumber;
}
