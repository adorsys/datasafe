package de.adorsys.datasafe.encrypiton.impl.pathencryption.dto;

import de.adorsys.datasafe.encrypiton.api.types.keystore.AuthPathEncryptionSecretKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.security.MessageDigest;

/**
 * Contains path segment for encryption or decryption and related secret key entity.
 */
@Getter
@Setter
@AllArgsConstructor
public class PathSegmentWithSecretKeyWith {

    /**
     * Digest to update with encrypted URI segment to authenticate.
     */
    private final MessageDigest digest;

    /**
     * Parent path hash.
     */
    private final byte[] parentHash;

    /**
     * Keys for encryption and decryption path.
     */
    private final AuthPathEncryptionSecretKey pathEncryptionSecretKey;

    /**
     * Encrypted or decrypted path segment.
     */
    private final String path;
}
