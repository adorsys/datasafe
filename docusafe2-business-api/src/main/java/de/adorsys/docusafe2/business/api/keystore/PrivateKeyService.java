package de.adorsys.docusafe2.business.api.keystore;

import de.adorsys.docusafe2.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

/**
 * Acts as a private keys database.
 */
public interface PrivateKeyService {

    KeyStoreAccess keystore(UserIdAuth forUser);
}
