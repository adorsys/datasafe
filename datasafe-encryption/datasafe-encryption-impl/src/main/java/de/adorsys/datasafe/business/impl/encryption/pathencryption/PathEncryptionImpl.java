package de.adorsys.datasafe.business.impl.encryption.pathencryption;

import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.encryption.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.business.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.business.api.types.utils.LogHelper;
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
        log.debug("encrypted path {} for user {} path {}", LogHelper.secure(encrypt.getPath()),
                LogHelper.secure(forUser.getUserID()), LogHelper.secure(path.getPath()));
        return encrypt;
    }

    @Override
    public URI decrypt(UserIDAuth forUser, URI path) {
        SecretKeyIDWithKey keySpec = privateKeyService.pathEncryptionSecretKey(forUser);
        URI decrypt = bucketPathEncryptionService.decrypt(keySpec.getSecretKey(), path);
        log.debug("decrypted path {} for user {} path {}", LogHelper.secure(decrypt.getPath()),
                LogHelper.secure(forUser.getUserID()), LogHelper.secure(path.getPath()));
        return decrypt;
    }
}
