package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.security.cert.X509Certificate;

/**
 * Wrapper for X509 certificate and its ID, so that public-private key pair can be found in keystore using this ID.
 */
@Getter
@ToString(of = "keyID")
@RequiredArgsConstructor
public class PublicKeyIDWithX509Cert {
    private final KeyID keyID;
    private final X509Certificate certificate;
}
