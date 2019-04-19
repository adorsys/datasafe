package de.adorsys.datasafe.business.api.encryption.bucketpathencryption;

import javax.crypto.spec.SecretKeySpec;
import java.net.URI;

public interface BucketPathEncryptionService {

    URI encrypt(SecretKeySpec secretKey, URI bucketPath);

    URI decrypt(SecretKeySpec secretKey, URI bucketPath);
}
