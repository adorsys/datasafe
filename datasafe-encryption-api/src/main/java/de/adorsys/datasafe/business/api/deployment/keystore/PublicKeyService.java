package de.adorsys.datasafe.business.api.deployment.keystore;

import de.adorsys.datasafe.business.api.deployment.keystore.types.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.types.UserID;

/**
 * Acts as a public keys database.
 */
public interface PublicKeyService {

    PublicKeyIDWithPublicKey publicKey(UserID forUser);
}
