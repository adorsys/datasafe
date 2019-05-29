package de.adorsys.datasafe.encrypiton.api.pathencryption.encryption;

import de.adorsys.datasafe.types.api.resource.Uri;

import javax.crypto.SecretKey;

public interface SymmetricPathEncryptionService {

    Uri encrypt(SecretKey secretKey, Uri bucketPath);
    Uri decrypt(SecretKey secretKey, Uri bucketPath);
}
