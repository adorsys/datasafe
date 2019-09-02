package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.crypto.SecretKey;

/**
 * Wrapper for secret key and its ID, so it can be found by ID within keystore.
 */
@Getter
@ToString
@RequiredArgsConstructor
public class SecretKeyIDWithKey {
    //TODO: rename to SecretKeyEntry

    private final KeyID keyID;
    private final SecretKey secretKey;
    private final Counter counter;
}
