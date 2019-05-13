package de.adorsys.datasafe.business.impl.profile.operations;

import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.version.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.version.types.keystore.ReadStorePassword;
import de.adorsys.datasafe.business.api.version.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.version.types.resource.DefaultPublicResource;
import de.adorsys.datasafe.business.api.version.types.resource.PublicResource;
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

    public KeyStoreAuth publicKeyStoreAuth() {
        return new KeyStoreAuth(
            new ReadStorePassword(config.keystorePassword()),
            new ReadKeyPassword(config.keystorePassword())
        );
    }

    public AbsoluteResourceLocation<PublicResource> dfsRoot() {
        return new AbsoluteResourceLocation<>(new DefaultPublicResource(config.systemRoot()));
    }
}
