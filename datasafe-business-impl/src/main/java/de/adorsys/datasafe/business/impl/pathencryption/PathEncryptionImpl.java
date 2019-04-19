package de.adorsys.datasafe.business.impl.pathencryption;

import de.adorsys.datasafe.business.api.deployment.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.types.UserIDAuth;

import javax.inject.Inject;
import java.net.URI;

public class PathEncryptionImpl implements PathEncryption {

    @Inject
    public PathEncryptionImpl() {
    }

    @Override
    public URI encrypt(UserIDAuth forUser, URI path) {
        return path;
    }

    @Override
    public URI decrypt(UserIDAuth forUser, URI path) {
        return path;
    }
}
