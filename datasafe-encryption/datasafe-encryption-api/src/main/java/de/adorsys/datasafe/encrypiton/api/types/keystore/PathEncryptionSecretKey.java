package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.*;

import javax.crypto.SecretKey;

@Getter
@ToString
@Value
@RequiredArgsConstructor
public class PathEncryptionSecretKey {

    @NonNull
    private final KeyID secretKeyId;

    @NonNull
    private final SecretKey secretKey;

    @NonNull
    private final KeyID counterKeyId;

    @NonNull
    private final SecretKey counterSecretKey;
}
