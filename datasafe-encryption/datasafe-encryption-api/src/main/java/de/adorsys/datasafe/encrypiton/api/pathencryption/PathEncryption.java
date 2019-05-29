package de.adorsys.datasafe.encrypiton.api.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.Uri;

public interface PathEncryption {

    Uri encrypt(UserIDAuth forUser, Uri path);
    Uri decrypt(UserIDAuth forUser, Uri path);
}
