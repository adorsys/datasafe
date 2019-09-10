package de.adorsys.datasafe.encrypiton.impl.pathencryption.dto;

import de.adorsys.datasafe.encrypiton.api.types.keystore.PathEncryptionSecretKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains data for encryption or decryption and related secret key entity
 *
 * {@code pathEncryptionSecretKey} keys for encryption and decryption path
 * {@code data} encrypted or decrypted data
 */
@Getter
@Setter
@AllArgsConstructor
public class PathSecretKeyWithData {
    private final PathEncryptionSecretKey pathEncryptionSecretKey;
    private final byte[] data;
}
