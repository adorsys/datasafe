package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;

import java.security.KeyStore;
import java.util.List;
import java.util.Map;

public interface KeyStoreCache {

    Map<UserID, List<PublicKeyIDWithPublicKey>> getPublicKeys();
    Map<UserID, KeyStore> getPrivateKeys();
}
