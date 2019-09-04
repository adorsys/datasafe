package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyEntry;

import java.security.KeyStore;
import java.util.List;
import java.util.Map;

/**
 * Cache delegating class to read users' public and private or secret keys.
 */
public interface KeyStoreCache {

    /**
     * Cache for users' public keys
     */
    Map<UserID, List<PublicKeyEntry>> getPublicKeys();

    /**
     * Cache for users' private/secret keys
     */
    Map<UserID, KeyStore> getKeystore();

    /**
     * Cache for users' storage access
     */
    Map<UserID, KeyStore> getStorageAccess();

    void remove(UserID forUser);
}
