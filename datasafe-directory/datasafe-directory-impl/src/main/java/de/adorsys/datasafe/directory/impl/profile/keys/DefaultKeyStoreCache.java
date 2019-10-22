package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.security.Key;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Default map-based private and public keys cache implementation. Quite safe to cache since they change infrequently.
 */
@Slf4j
@RuntimeDelegate
public class DefaultKeyStoreCache implements KeyStoreCache {

    private final Map<UserID, List<PublicKeyIDWithPublicKey>> publicKeys;
    private final Map<UserID, KeyStore> keystore;
    private final Map<UserID, KeyStore> storageAccess;

    @Inject
    public DefaultKeyStoreCache(
            Map<UserID, List<PublicKeyIDWithPublicKey>> publicKeys,
            Map<UserID, KeyStore> keystore,
            Map<UserID, KeyStore> storageAccess) {
        this.publicKeys = publicKeys;
        this.keystore = keystore;
        this.storageAccess = storageAccess;
    }

    @Override
    public Map<UserID, List<PublicKeyIDWithPublicKey>> getPublicKeys() {
        return publicKeys;
    }

    @Override
    public Map<UserID, KeyStore> getKeystore() {
        return keystore;
    }

    @Override
    public Map<UserID, KeyStore> getStorageAccess() {
        return storageAccess;
    }

    @Override
    public KeyStore computeIfAbsent(UserIDAuth userIDAuth, Function<? super UserID, ? extends KeyStore> mappingFunction) {
        return getKeystore().computeIfAbsent(userIDAuth.getUserID(), userID -> {
            KeyStore keyStore = mappingFunction.apply(userIDAuth.getUserID());
            if (keyStore.getType().equals("UBER")) {
                getKeystore().put(userIDAuth.getUserID(), keyStore);
                return keyStore;
            }
            return convertKeyStoreToUberKeyStore(userIDAuth, keyStore);
        });

    }

    @Override
    public void remove(UserID userID) {

        publicKeys.remove(userID);
        keystore.remove(userID);
        storageAccess.remove(userID);
    }


    @SneakyThrows
    private KeyStore convertKeyStoreToUberKeyStore(UserIDAuth currentCredentials, KeyStore current) {
        log.debug("the keystore is of type {} and will be converted to uber in cache", current.getType() );

        KeyStore newKeystore = KeyStore.getInstance("UBER");
        newKeystore.load(null, null);
        Enumeration<String> aliases = current.aliases();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Key currentKey = current.getKey(alias, currentCredentials.getReadKeyPassword().getValue());
            newKeystore.setKeyEntry(
                    alias,
                    currentKey,
                    currentCredentials.getReadKeyPassword().getValue(),
                    current.getCertificateChain(alias)
            );
        }

        return newKeystore;
    }

}
