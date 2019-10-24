package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@AllArgsConstructor
public class UserKeyStoreCache {
    @Delegate (excludes = ExcludeComputeIfAbsent.class)
    private final Map<UserID, KeyStore> map;


    /*
      The original Map.computeIfAbsent method must not be called and thus is not provided
      by this class! It is task of the DefaultKeyStoreCache to generate a new Uber Key Store
      for the cache only. For that the password needed, which is
      not provided in this method here, is expected too. See next method.

      public KeyStore computeIfAbsent(UserID key, Function<? super UserID, ? extends KeyStore> mappingFunction) {}
     */

    /**
     * If the keyStore provided is a not a UBER KeyStore, a new UBER KeyStore without a
     * ReadStorePassword is created. This store is returned and stored in the map.
     *
     * @param userIDAuth
     * @param mappingFunction
     * @return always a UBER KeyStore
     */
    public KeyStore computeIfAbsent(UserIDAuth userIDAuth, Function<? super UserID, ? extends KeyStore> mappingFunction) {
        if (map.containsKey(userIDAuth.getUserID())) {
            return map.get(userIDAuth.getUserID());
        }
        KeyStore keyStore = mappingFunction.apply(userIDAuth.getUserID());
        if (! "UBER".equals(keyStore.getType())) {
            keyStore = convertKeyStoreToUberKeyStore(userIDAuth, keyStore);
        }
        map.put(userIDAuth.getUserID(), keyStore);
        return keyStore;

    }

    /**
     *  Does the conversion of any keyStore to a UBER KeyStore.
     *
     * @param currentCredentials
     * @param current
     * @return
     */
    @SneakyThrows
    private KeyStore convertKeyStoreToUberKeyStore(UserIDAuth currentCredentials, KeyStore current) {
        log.debug("the keystore for user {} is of type {} and will be converted to uber in cache", currentCredentials.getUserID(), current.getType() );

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

    private interface ExcludeComputeIfAbsent<K,V> {
        KeyStore computeIfAbsent(UserID key, Function<? super UserID, ? extends KeyStore> mappingFunction);
    }
}
