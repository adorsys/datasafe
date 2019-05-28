package de.adorsys.datasafe.encrypiton.api.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;

import java.net.URI;

public interface PathEncryption {

    URI encrypt(UserIDAuth forUser, URI path);
    URI decrypt(UserIDAuth forUser, URI path);
}
