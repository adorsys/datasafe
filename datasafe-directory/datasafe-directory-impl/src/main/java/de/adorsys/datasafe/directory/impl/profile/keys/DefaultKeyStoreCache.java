package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyEntry;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.Getter;

import javax.inject.Inject;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;

/**
 * Default map-based private and public keys cache implementation. Quite safe to cache since they change infrequently.
 */
@Getter
@RuntimeDelegate
public class DefaultKeyStoreCache implements KeyStoreCache {

    private final Map<UserID, List<PublicKeyEntry>> publicKeys;
    private final Map<UserID, KeyStore> keystore;
    private final Map<UserID, KeyStore> storageAccess;

    @Inject
    public DefaultKeyStoreCache(
            Map<UserID, List<PublicKeyEntry>> publicKeys,
            Map<UserID, KeyStore> keystore,
            Map<UserID, KeyStore> storageAccess) {
        this.publicKeys = publicKeys;
        this.keystore = keystore;
        this.storageAccess = storageAccess;
    }

    @Override
    public void remove(UserID userID) {

        publicKeys.remove(userID);
        keystore.remove(userID);
        storageAccess.remove(userID);
    }
}
