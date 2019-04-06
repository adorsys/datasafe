package de.adorsys.docusafe2.business.api.bucketpathencryption;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;

import javax.crypto.SecretKey;

public interface BucketPathEncryptionService {

    BucketPath encrypt(SecretKey secretKey, BucketPath bucketPath);

    BucketPath decrypt(SecretKey secretKey, BucketPath bucketPath);
}
