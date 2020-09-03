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
public class NoPathEncryptionImpl extends PathEncryptionImpl {

    @Inject
    public NoPathEncryptionImpl(
        SymmetricPathEncryptionService symmetricPathEncryptionService,
        PrivateKeyService privateKeyService) {
        super(symmetricPathEncryptionService, privateKeyService);
    }

    @Override
    public Uri encrypt(UserIDAuth forUser, Uri path) {
        return path;
    }

    @Override
    public Function<Uri, Uri> decryptor(UserIDAuth forUser) {
        return Function.identity();
    }
}
