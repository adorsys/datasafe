package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.security.PublicKey;

@Getter
@ToString
@RequiredArgsConstructor
public class PublicKeyIDWithPublicKey {

    private final KeyID keyID;
    private final PublicKey publicKey;
}
