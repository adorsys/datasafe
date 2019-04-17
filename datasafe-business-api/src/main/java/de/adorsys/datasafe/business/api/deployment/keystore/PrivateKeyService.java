package de.adorsys.datasafe.business.api.deployment.keystore;

import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.UserIDAuth;

/**
 * Acts as a private keys database.
 */
public interface PrivateKeyService {

    KeyStoreAccess keystore(UserIDAuth forUser);
}
