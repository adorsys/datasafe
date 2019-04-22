package de.adorsys.datasafe.business.api.deployment.pathencryption;

import de.adorsys.datasafe.business.api.types.UserIDAuth;

import java.net.URI;

public interface PathEncryption {

    URI encrypt(UserIDAuth forUser, URI path);
    URI decrypt(UserIDAuth forUser, URI path);
}
