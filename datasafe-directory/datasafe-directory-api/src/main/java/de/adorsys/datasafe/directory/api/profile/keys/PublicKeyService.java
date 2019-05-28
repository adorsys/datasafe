package de.adorsys.datasafe.directory.api.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;

/**
 * Acts as a public keys database.
 */
public interface PublicKeyService {

    PublicKeyIDWithPublicKey publicKey(UserID forUser);
}
