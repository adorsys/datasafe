package de.adorsys.datasafe.business.impl.pathencryption;

import de.adorsys.datasafe.business.api.deployment.keystore.PrivateKeyService;
import de.adorsys.datasafe.business.api.deployment.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.encryption.bucketpathencryption.BucketPathEncryptionService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;

import javax.inject.Inject;
import java.net.URI;

public class PathEncryptionImpl implements PathEncryption {

    private final BucketPathEncryptionService bucketPathEncryptionService;
    private final PrivateKeyService privateKeyService;

    @Inject
    public PathEncryptionImpl(BucketPathEncryptionService bucketPathEncryptionService, PrivateKeyService privateKeyService) {
        this.bucketPathEncryptionService = bucketPathEncryptionService;
        this.privateKeyService = privateKeyService;
    }

    @Override
    public URI encrypt(UserIDAuth forUser, URI path) {
        return path;
    }

    @Override
    public URI decrypt(UserIDAuth forUser, URI path) {
        return path;
    }
}
