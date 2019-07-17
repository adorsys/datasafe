package de.adorsys.datasafe.directory.api.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;

import java.security.Key;
import java.util.Set;

/**
 * This class is responsible for accessing/updating/creating users' keystore on higher level.
 */
public interface KeyStoreOperations {

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
