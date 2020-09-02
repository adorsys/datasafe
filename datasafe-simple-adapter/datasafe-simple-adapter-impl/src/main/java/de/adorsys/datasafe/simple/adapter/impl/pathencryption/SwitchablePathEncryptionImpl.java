package de.adorsys.datasafe.simple.adapter.impl.pathencryption;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.function.Function;

@Slf4j
public class SwitchablePathEncryptionImpl extends PathEncryptionImpl {

    private boolean withPathEncryption;

    @Inject
    public SwitchablePathEncryptionImpl(
        Boolean withPathEncryption,
        SymmetricPathEncryptionService symmetricPathEncryptionService,
        PrivateKeyService privateKeyService) {
        super(symmetricPathEncryptionService, privateKeyService);
        this.withPathEncryption = withPathEncryption;
    }

    @Override
    public Uri encrypt(UserIDAuth forUser, Uri path) {
        if (withPathEncryption) {
            log.info("WITH PATH ENCRYPTION TRUE");
            return super.encrypt(forUser, path);
        }
        log.info("WITH PATH ENCRYPTION FALSE");
        return path;
    }

    @Override
    public Function<Uri, Uri> decryptor(UserIDAuth forUser) {
        if (withPathEncryption) {
            return super.decryptor(forUser);
        }
        return Function.identity();
    }
}
