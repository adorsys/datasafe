package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.security.PublicKey;

/**
 * Wrapper for public key and its ID, so that public-private key pair can be found in keystore using this ID.
 */
@Getter
@ToString(of = "keyID")
@RequiredArgsConstructor
@EqualsAndHashCode
public class PublicKeyIDWithPublicKey {
    private final KeyID keyID;
    private final PublicKey publicKey;
}
