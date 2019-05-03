package de.adorsys.datasafe.business.api.encryption.pathencryption.encryption;

import javax.crypto.SecretKey;
import java.net.URI;

public interface SymmetricPathEncryptionService {

    URI encrypt(SecretKey secretKey, URI bucketPath);
    URI decrypt(SecretKey secretKey, URI bucketPath);
}
