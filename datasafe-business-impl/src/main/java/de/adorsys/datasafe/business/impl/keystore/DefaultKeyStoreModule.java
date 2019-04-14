package de.adorsys.datasafe.business.impl.keystore;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.deployment.keystore.KeyStoreService;
import de.adorsys.datasafe.business.impl.keystore.service.KeyStoreServiceImpl;

@Module
public abstract class DefaultKeyStoreModule {

    @Binds
    public abstract KeyStoreService keyStoreService(KeyStoreServiceImpl impl);
}
