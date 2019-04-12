package de.adorsys.datasafe.business.api.deployment.keystore;

import de.adorsys.datasafe.business.api.types.PublicKeyWithId;
import de.adorsys.datasafe.business.api.types.UserID;

/**
 * Acts as a public keys database.
 */
public interface PublicKeyService {

    PublicKeyWithId publicKey(UserID forUser);
}
