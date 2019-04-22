package de.adorsys.datasafe.business.api.directory.profile.keys;

import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.UserIDAuth;

/**
 * Acts as a private keys database.
 */
public interface PrivateKeyService {

    KeyStoreAccess keystore(UserIDAuth forUser);
}
