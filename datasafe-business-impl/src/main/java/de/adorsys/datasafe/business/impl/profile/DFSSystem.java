package de.adorsys.datasafe.business.impl.profile;

import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.keystore.ReadStorePassword;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.SystemCredentials;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.net.URI;

public class DFSSystem {

    private static final String PATH = "system";
    private static final String SYS_PASSWORD = "system-store-password";

    public static final String CREDS_ID = "SYS-001";

    @Inject
    public DFSSystem() {
    }

    @SneakyThrows
    public DFSAccess systemDfs() {

        return DFSAccess.builder()
            .logicalPath(new URI(PATH))
            .physicalPath(new URI(PATH))
            .credentials(SystemCredentials.builder().id(CREDS_ID).build())
            .build();
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
