package de.adorsys.datasafe.business.impl.profile.keys;

import de.adorsys.datasafe.business.api.version.types.UserID;

import java.security.KeyStore;
import java.util.Map;

public interface KeyStoreCache {

    Map<UserID, KeyStore> getPublicKeys();
    Map<UserID, KeyStore> getPrivateKeys();
}
