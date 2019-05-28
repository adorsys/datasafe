package de.adorsys.datasafe.directory.api.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;

import java.security.Key;

/**
 * Acts as a private keys database.
 */
public interface PrivateKeyService {

    SecretKeyIDWithKey pathEncryptionSecretKey(UserIDAuth forUser);
    SecretKeyIDWithKey documentEncryptionSecretKey(UserIDAuth forUser);
    Key keyById(UserIDAuth forUser, String keyId);
}
