package de.adorsys.datasafe.business.impl.profile;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.keystore.ReadStorePassword;
import lombok.SneakyThrows;

import javax.inject.Inject;

public class DFSSystem {

    private static final String SYS_PASSWORD = "system-store-password";

    public static final String CREDS_ID = "SYS-001";

    @Inject
    public DFSSystem() {
    }

    @SneakyThrows
    public String systemKeystoreAuth() {
        return SYS_PASSWORD;
    }

    public KeyStoreAuth privateKeyStoreAuth(UserIDAuth auth) {

        return new KeyStoreAuth(
            new ReadStorePassword(SYS_PASSWORD),
            auth.getReadKeyPassword()
        );
    }

    public KeyStoreAuth publicKeyStoreAuth() {

        return new KeyStoreAuth(
            new ReadStorePassword(SYS_PASSWORD),
            new ReadKeyPassword(SYS_PASSWORD)
        );
    }
}
