package de.adorsys.datasafe.business.api.profile.keys;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.keystore.PublicKeyIDWithPublicKey;

/**
 * Acts as a public keys database.
 */
public interface PublicKeyService {

    PublicKeyIDWithPublicKey publicKey(UserID forUser);
}
