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

public class DFSSystem {

    private final DFSConfig config;

    @Inject
    public DFSSystem(DFSConfig config) {
        this.config = config;
    }

    @SneakyThrows
    public ReadStorePassword systemKeystoreAuth() {
        return new ReadStorePassword(config.keystorePassword());
    }

    public KeyStoreAuth privateKeyStoreAuth(UserIDAuth auth) {

        return new KeyStoreAuth(
            new ReadStorePassword(config.keystorePassword()),
            auth.getReadKeyPassword()
        );
    }

    public AbsoluteLocation<PublicResource> dfsRoot() {
        return new AbsoluteLocation<>(new BasePublicResource(config.systemRoot()));
    }
}
