package de.adorsys.datasafe.types.api.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PathEncryptionVersion {

    AES_SIV("SIV");


    // Should be 3-symbol string. Always added at the end of encrypted path
    private final String name;
}
