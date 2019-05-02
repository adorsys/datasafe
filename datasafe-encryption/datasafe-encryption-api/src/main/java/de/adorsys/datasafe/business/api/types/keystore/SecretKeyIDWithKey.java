package de.adorsys.datasafe.business.api.types.keystore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.crypto.SecretKey;

@Getter
@ToString
@RequiredArgsConstructor
public class SecretKeyIDWithKey {
    private final KeyID keyID;
    private final SecretKey secretKey;
}
