package de.adorsys.datasafe.directory.api.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;

/**
 * Acts as a public keys database.
 */
public interface PublicKeyService {

    /**
     * Get users' public key that can be used for asymmetric encryption of document sent to INBOX.
     * @param forUser User who owns public key (and has its private key pair)
     * @return Public key for document sharing.
     */
    PublicKeyIDWithPublicKey publicKey(UserID forUser);
}
