package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.*;

@Getter
@ToString
@Value
@RequiredArgsConstructor
public class PathEncryptionSecretKey {

    @NonNull
    private final SecretKeyIDWithKey secretKey;

    @NonNull
    private final SecretKeyIDWithKey counterSecretKey;
}
