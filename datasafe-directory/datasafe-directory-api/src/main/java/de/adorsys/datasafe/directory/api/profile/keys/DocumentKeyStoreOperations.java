package de.adorsys.datasafe.directory.api.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;

import java.security.Key;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for accessing/updating/creating users' keystore that contains keys for
 * DOCUMENT access on higher level.
 */
public interface DocumentKeyStoreOperations {

    /**
     * Creates keystore and returns public keys from it.
     * @param forUser Keystore owner
     * @return Created public keys from keystore.
     */
    List<PublicKeyIDWithPublicKey> createAndWriteKeyStore(UserIDAuth forUser);

    /**
     * Updates ReadKeyPassword for users' keystore. Clears ALL cached keystores.
     * @param forUser Keystore owner.
     * @param newPassword New ReadKeyStore password.
     */
    void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword);

    /**
     * Read key from the keystore associated with user.
     * @param forUser keystore owner
     * @param alias key alias to read
     * @return Key from keystore.
     */
    Key getKey(UserIDAuth forUser, String alias);

    /**
     * Aliases of keys stored in keystore associated with user.
     * @param forUser keystore owner.
     * @return Key aliases from keystore.
     */
    Set<String> readAliases(UserIDAuth forUser);
}
