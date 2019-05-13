package de.adorsys.datasafe.business.impl.encryption.pathencryption;

import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.encryption.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.business.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.keystore.SecretKeyIDWithKey;

import javax.inject.Inject;
import java.net.URI;

public class PathEncryptionImpl implements PathEncryption {

    private final SymmetricPathEncryptionService bucketPathEncryptionService;
    private final PrivateKeyService privateKeyService;

    @Inject
    public PathEncryptionImpl(SymmetricPathEncryptionService bucketPathEncryptionService,
                              PrivateKeyService privateKeyService) {
        this.bucketPathEncryptionService = bucketPathEncryptionService;
        this.privateKeyService = privateKeyService;
    }

    @Override
    public URI encrypt(UserIDAuth forUser, URI path) {
        SecretKeyIDWithKey keySpec = privateKeyService.pathEncryptionSecretKey(forUser);
        return bucketPathEncryptionService.encrypt(keySpec.getSecretKey(), path);
    }

    @Override
    public URI decrypt(UserIDAuth forUser, URI path) {
        SecretKeyIDWithKey keySpec = privateKeyService.pathEncryptionSecretKey(forUser);
        return bucketPathEncryptionService.decrypt(keySpec.getSecretKey(), path);
    }
}
