package de.adorsys.docusafe2.business.api.keystore;

import de.adorsys.docusafe2.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

public interface PrivateKeyService {

    KeyStoreAccess keystore(UserIdAuth forUser);
}
