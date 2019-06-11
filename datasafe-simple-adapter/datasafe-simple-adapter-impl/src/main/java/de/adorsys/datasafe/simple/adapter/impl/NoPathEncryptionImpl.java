package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.Uri;

public class NoPathEncryptionImpl implements PathEncryption {
    @Override
    public Uri encrypt(UserIDAuth forUser, Uri path) {
        return path;
    }

    @Override
    public Uri decrypt(UserIDAuth forUser, Uri path) {
        return path;
    }
}
