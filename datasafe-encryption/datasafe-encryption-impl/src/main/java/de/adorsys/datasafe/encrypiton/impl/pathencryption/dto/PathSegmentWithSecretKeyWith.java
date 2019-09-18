package de.adorsys.datasafe.encrypiton.impl.pathencryption.dto;

import de.adorsys.datasafe.encrypiton.api.types.keystore.AuthPathEncryptionSecretKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains path segment for encryption or decryption and related secret key entity.
 */
@Getter
@Setter
@AllArgsConstructor
public class PathSegmentWithSecretKeyWith {

    /**
     * Keys for encryption and decryption path.
     */
    private final AuthPathEncryptionSecretKey pathEncryptionSecretKey;

    /**
     * Path segment position to authenticate.
     */
    private final int authenticationPosition;

    /**
     * Encrypted or decrypted path segment.
     */
    private final String path;
}
