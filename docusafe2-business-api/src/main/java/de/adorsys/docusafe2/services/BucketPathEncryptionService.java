package de.adorsys.docusafe2.services;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.keystore.api.types.KeyID;
import de.adorsys.docusafe2.keystore.api.types.KeyStoreAccess;

import java.security.PublicKey;


public interface BucketPathEncryptionService {
    BucketPath encrypt(PublicKey publicKey, KeyID keyid, BucketPath bucketPath);
    BucketPath decrypt(KeyStoreAccess keyStoreAccess, BucketPath bucketPath);
}
