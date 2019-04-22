package de.adorsys.datasafe.business.api.directory.profile.keys;

import de.adorsys.datasafe.business.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.types.UserID;

/**
 * Acts as a public keys database.
 */
public interface PublicKeyService {

    PublicKeyIDWithPublicKey publicKey(UserID forUser);
}
