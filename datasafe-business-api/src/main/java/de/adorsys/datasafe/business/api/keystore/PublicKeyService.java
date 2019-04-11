package de.adorsys.datasafe.business.api.keystore;

import de.adorsys.datasafe.business.api.types.PublicKeyWithId;
import de.adorsys.docusafe2.business.api.types.UserId;

/**
 * Acts as a public keys database.
 */
public interface PublicKeyService {

    PublicKeyWithId publicKey(UserId forUser);
}
