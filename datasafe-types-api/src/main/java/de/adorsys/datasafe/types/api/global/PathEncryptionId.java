package de.adorsys.datasafe.types.api.global;

import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.Getter;

/**
 * Identifies what algorithm was used to encrypt path.
 */
@Getter
public enum PathEncryptionId {

    AES_SIV("SIV");

    // Should be 3-symbol string
    private final String name;

    PathEncryptionId(String name) {
        if (name.length() > 3) {
            throw new IllegalArgumentException("Too long encryption identifier name, 3 characters expected");
        }

        this.name = name;
    }

    public Uri asUriRoot() {
        return new Uri(name + "/");
    }
}
