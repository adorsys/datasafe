package de.adorsys.docusafe2.service;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;

import javax.crypto.spec.SecretKeySpec;

public interface BucketPathEncryptionService {

    BucketPath encrypt(SecretKeySpec secretKey, BucketPath bucketPath);

    BucketPath decrypt(SecretKeySpec secretKey, BucketPath bucketPath);
}
