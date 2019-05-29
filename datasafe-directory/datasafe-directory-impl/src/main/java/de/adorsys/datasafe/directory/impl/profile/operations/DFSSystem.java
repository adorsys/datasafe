package de.adorsys.datasafe.directory.impl.profile.operations;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadStorePassword;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.SneakyThrows;

import javax.inject.Inject;

/**
 * Class that provides system root location, system DFS access credentials
 * and enhances user credentials with keystore opening credentials.
 */
public class DFSSystem {

    private final DFSConfig config;

    @Inject
    public DFSSystem(DFSConfig config) {
        this.config = config;
    }

    /**
     * Get credentials to read key in users' keystore - enhance user password with password to open keystore.
     */
    public KeyStoreAuth privateKeyStoreAuth(UserIDAuth auth) {
        return new KeyStoreAuth(
                systemKeystoreAuth(),
                auth.getReadKeyPassword()
        );
    }

    /**
     * Where system files like users' private and public profile are located within DFS.
     */
    public AbsoluteLocation<PublicResource> dfsRoot() {
        return new AbsoluteLocation<>(new BasePublicResource(config.systemRoot()));
    }

    /**
     * Get credentials to open and serialize/deserialize keystore.
     */
    @SneakyThrows
    private ReadStorePassword systemKeystoreAuth() {
        return new ReadStorePassword(config.keystorePassword());
    }
}
