package de.adorsys.datasafe.business.impl.profile.keys;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.keystore.PublicKeyIDWithPublicKey;

import java.security.KeyStore;
import java.util.List;
import java.util.Map;

public interface KeyStoreCache {

    Map<UserID, List<PublicKeyIDWithPublicKey>> getPublicKeys();
    Map<UserID, KeyStore> getPrivateKeys();
}
