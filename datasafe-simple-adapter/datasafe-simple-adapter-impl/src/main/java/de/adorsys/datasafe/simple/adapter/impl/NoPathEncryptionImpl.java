package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImplRuntimeDelegatable;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.resource.Uri;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class NoPathEncryptionImpl extends PathEncryptionImplRuntimeDelegatable {

    @Inject
    public NoPathEncryptionImpl(@Nullable OverridesRegistry context, SymmetricPathEncryptionService bucketPathEncryptionService, PrivateKeyService privateKeyService) {
        super(context, bucketPathEncryptionService, privateKeyService);
    }

    @Override
    public Uri encrypt(UserIDAuth forUser, Uri path) {
        return path;
    }

    @Override
    public Uri decrypt(UserIDAuth forUser, Uri path) {
        return path;
    }
}
