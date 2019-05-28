package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.utils.Log;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.net.URI;

@Slf4j
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
        URI encrypt = bucketPathEncryptionService.encrypt(keySpec.getSecretKey(), path);
        log.debug("encrypted path {} for user {} path {}", Log.secure(encrypt.getPath()),
                Log.secure(forUser.getUserID()), Log.secure(path.getPath()));
        return encrypt;
    }

    @Override
    public URI decrypt(UserIDAuth forUser, URI path) {
        SecretKeyIDWithKey keySpec = privateKeyService.pathEncryptionSecretKey(forUser);
        URI decrypt = bucketPathEncryptionService.decrypt(keySpec.getSecretKey(), path);
        log.debug("decrypted path {} for user {} path {}", Log.secure(decrypt.getPath()),
                Log.secure(forUser.getUserID()), Log.secure(path.getPath()));
        return decrypt;
    }
}
