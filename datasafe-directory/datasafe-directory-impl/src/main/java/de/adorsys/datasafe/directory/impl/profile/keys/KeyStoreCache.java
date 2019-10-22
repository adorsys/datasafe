package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import org.checkerframework.checker.units.qual.K;

import java.security.KeyStore;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Cache delegating class to read users' public and private or secret keys.
 */
public interface KeyStoreCache {

    /**
     * Cache for users' public keys
     */
    Map<UserID, List<PublicKeyIDWithPublicKey>> getPublicKeys();

    /**
     * Cache for users' private/secret keys
     */
    Map<UserID, KeyStore> getKeystore();

    /**
     * Cache for users' storage access
     */
    Map<UserID, KeyStore> getStorageAccess();

    /**
     * expects any keystore. Rather than storing the store as it is,
     * it will be converted to a UBER keystore to save performance in
     * decrypting the store.
     * The returned KeyStore is added to the getKeyStore() Map.
     */
    KeyStore computeIfAbsent(UserIDAuth userIDAuth, Function<? super UserID, ? extends KeyStore> mappingFunction);

    void remove(UserID forUser);
}
