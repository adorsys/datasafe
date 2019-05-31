package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.KeyStore;
import java.util.List;
import java.util.Map;

/**
 * Default map-based private and public keys cache implementation. Quite safe to cache since they change infrequently.
 */
@Getter
@RequiredArgsConstructor
public class DefaultKeyStoreCache implements KeyStoreCache {

    private final Map<UserID, List<PublicKeyIDWithPublicKey>> publicKeys;
    private final Map<UserID, KeyStore> keystore;
}
