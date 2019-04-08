package de.adorsys.docusafe2.business.impl.credentials;

import de.adorsys.docusafe2.business.api.keystore.PrivateKeyService;
import de.adorsys.docusafe2.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

public class PrivateKeyServiceImpl implements PrivateKeyService {

    @Override
    public KeyStoreAccess keystore(UserIdAuth forUser) {
        return null;
    }
}
