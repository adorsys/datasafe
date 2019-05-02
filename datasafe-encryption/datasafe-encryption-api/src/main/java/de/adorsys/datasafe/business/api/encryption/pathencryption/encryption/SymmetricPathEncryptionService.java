package de.adorsys.datasafe.business.api.encryption.pathencryption.encryption;

import javax.crypto.spec.SecretKeySpec;
import java.net.URI;

public interface SymmetricPathEncryptionService {

    URI encrypt(SecretKeySpec secretKey, URI bucketPath);
    URI decrypt(SecretKeySpec secretKey, URI bucketPath);
}
