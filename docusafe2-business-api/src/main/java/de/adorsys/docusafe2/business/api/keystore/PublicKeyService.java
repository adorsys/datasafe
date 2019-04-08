package de.adorsys.docusafe2.business.api.keystore;

import de.adorsys.docusafe2.business.api.types.PublicKeyWithId;
import de.adorsys.docusafe2.business.api.types.UserId;

public interface PublicKeyService {

    PublicKeyWithId publicKey(UserId forUser);
}
