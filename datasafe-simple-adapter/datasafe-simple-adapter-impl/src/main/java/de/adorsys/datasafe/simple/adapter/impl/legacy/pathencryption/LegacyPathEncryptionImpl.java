package de.adorsys.datasafe.simple.adapter.impl.legacy.pathencryption;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PathEncryptionSecretKey;
import de.adorsys.datasafe.simple.adapter.api.legacy.pathencryption.LegacySymmetricPathEncryptionService;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.function.Function;

/**
 * Default path encryption service that uses {@link PrivateKeyService#pathEncryptionSecretKey(UserIDAuth)} as
 * path encryption key.
 */
@Slf4j
public class LegacyPathEncryptionImpl implements PathEncryption {

    private final LegacySymmetricPathEncryptionService bucketPathEncryptionService;
    private final PrivateKeyService privateKeyService;

    @Inject
    public LegacyPathEncryptionImpl(LegacySymmetricPathEncryptionService bucketPathEncryptionService,
                                    PrivateKeyService privateKeyService) {
        this.bucketPathEncryptionService = bucketPathEncryptionService;
        this.privateKeyService = privateKeyService;
    }

    /**
     * Simply pipes {@link LegacySymmetricPathEncryptionService} and {@link PrivateKeyService} to encrypt URI
     */
    @Override
    public Uri encrypt(UserIDAuth forUser, Uri path) {
        PathEncryptionSecretKey keySpec = privateKeyService.pathEncryptionSecretKey(forUser);
        Uri encrypt = bucketPathEncryptionService.encrypt(keySpec.getSecretKey().getSecretKey(), path);
        log.debug("encrypted path {} for user {} path {}", encrypt, forUser.getUserID(), path);
        return encrypt;
    }

    /**
     * Simply pipes {@link LegacySymmetricPathEncryptionService} and {@link PrivateKeyService} to decrypt URI
     */
    @Override
    public Function<Uri, Uri> decryptor(UserIDAuth forUser) {
        PathEncryptionSecretKey keySpec = privateKeyService.pathEncryptionSecretKey(forUser);
        return encryptedPath -> {
            Uri decrypt = bucketPathEncryptionService.decrypt(keySpec.getSecretKey().getSecretKey(), encryptedPath);
            log.debug("decrypted path {} for user {} path {}", decrypt, forUser.getUserID(), encryptedPath);
            return decrypt;
        };
    }
}
