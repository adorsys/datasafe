package de.adorsys.datasafe.business.impl.profile.keys;

import de.adorsys.datasafe.business.api.version.types.UserID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.KeyStore;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class DefaultKeyStoreCache implements KeyStoreCache {

    private final Map<UserID, KeyStore> privateKeys;
    private final Map<UserID, KeyStore> publicKeys;
}
