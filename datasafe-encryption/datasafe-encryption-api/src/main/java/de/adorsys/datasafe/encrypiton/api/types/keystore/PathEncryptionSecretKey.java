package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.crypto.SecretKey;

@Getter
@ToString
@RequiredArgsConstructor
public class PathEncryptionSecretKey {

    private final KeyID secretKeyId;
    private final SecretKey secretKey;
    private final KeyID counterKeyId;
    private final SecretKey counterSecretKey;
}
