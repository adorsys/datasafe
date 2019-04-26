package de.adorsys.datasafe.business.api.encryption.bucketpathencryption;

import javax.crypto.SecretKey;
import java.net.URI;

public interface BucketPathEncryptionService {

    URI encrypt(SecretKey secretKey, URI bucketPath);

    URI decrypt(SecretKey secretKey, URI bucketPath);
}
