package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.utils.Log;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * Default path encryption service that uses {@link PrivateKeyService#pathEncryptionSecretKey(UserIDAuth)} as
 * path encryption key.
 */
@Slf4j
@RuntimeDelegate
public class PathEncryptionImpl implements PathEncryption {

    private final SymmetricPathEncryptionService bucketPathEncryptionService;
    private final PrivateKeyService privateKeyService;

    @Inject
    public PathEncryptionImpl(SymmetricPathEncryptionService bucketPathEncryptionService,
                              PrivateKeyService privateKeyService) {
        this.bucketPathEncryptionService = bucketPathEncryptionService;
        this.privateKeyService = privateKeyService;
    }

    /**
     * Simply pipes {@link SymmetricPathEncryptionService} and {@link PrivateKeyService} to encrypt URI
     */
    @Override
    public Uri encrypt(UserIDAuth forUser, Uri path) {
        SecretKeyIDWithKey keySpec = privateKeyService.pathEncryptionSecretKey(forUser);
        Uri encrypt = bucketPathEncryptionService.encrypt(keySpec.getSecretKey(), path);
        log.debug("encrypted path {} for user {} path {}", Log.secure(encrypt.getPath()),
                Log.secure(forUser.getUserID()), Log.secure(path.getPath()));
        return encrypt;
    }

    /**
     * Simply pipes {@link SymmetricPathEncryptionService} and {@link PrivateKeyService} to decrypt URI
     */
    @Override
    public Uri decrypt(UserIDAuth forUser, Uri path) {
        SecretKeyIDWithKey keySpec = privateKeyService.pathEncryptionSecretKey(forUser);
        Uri decrypt = bucketPathEncryptionService.decrypt(keySpec.getSecretKey(), path);
        log.debug("decrypted path {} for user {} path {}", Log.secure(decrypt.getPath()),
                Log.secure(forUser.getUserID()), Log.secure(path.getPath()));
        return decrypt;
    }
}
