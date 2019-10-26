package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.AuthPathEncryptionSecretKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.function.Function;

/**
 * Default path encryption service that uses {@link PrivateKeyService#pathEncryptionSecretKey(UserIDAuth)} as
 * path encryption key.
 */
@Slf4j
@RuntimeDelegate
public class PathEncryptionImpl implements PathEncryption {

    private final SymmetricPathEncryptionService symmetricPathEncryptionService;
    private final PrivateKeyService privateKeyService;

    @Inject
    public PathEncryptionImpl(SymmetricPathEncryptionService symmetricPathEncryptionService,
                              PrivateKeyService privateKeyService) {
        this.symmetricPathEncryptionService = symmetricPathEncryptionService;
        this.privateKeyService = privateKeyService;
    }

    /**
     * Simply pipes {@link SymmetricPathEncryptionService} and {@link PrivateKeyService} to encrypt URI
     */
    @Override
    public Uri encrypt(UserIDAuth forUser, Uri path) {
        AuthPathEncryptionSecretKey pathEncryptionSecretKey = privateKeyService.pathEncryptionSecretKey(forUser);
        Uri encrypt = symmetricPathEncryptionService.encrypt(pathEncryptionSecretKey, path);
        log.debug("encrypted path {} for user {} path {}", encrypt, forUser.getUserID(), path);
        return encrypt;
    }

    /**
     * Simply pipes {@link SymmetricPathEncryptionService} and {@link PrivateKeyService} to decrypt URI
     */
    @Override
    public Function<Uri, Uri> decryptor(UserIDAuth forUser) {
        AuthPathEncryptionSecretKey pathEncryptionSecretKey = privateKeyService.pathEncryptionSecretKey(forUser);
        return encryptedPath -> {
            Uri decrypt = symmetricPathEncryptionService.decrypt(pathEncryptionSecretKey, encryptedPath);
            log.debug("decrypted path {} for user {} path {}", decrypt, forUser.getUserID(), encryptedPath);
            return decrypt;
        };
    }
}
