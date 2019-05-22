package de.adorsys.datasafe.business.impl.privatestore.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.types.cobertura.CoberturaIgnore;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceServiceImpl;
import de.adorsys.datasafe.business.impl.privatespace.actions.*;

/**
 * This module is responsible for providing default actions on PRIVATE folder.
 */
@Module
public abstract class DefaultPrivateActionsModule {

    @CoberturaIgnore
    @Binds
    abstract EncryptedResourceResolver encryptedResourceResolver(EncryptedResourceResolverImpl impl);

    @CoberturaIgnore
    @Binds
    abstract ListPrivate listPrivate(ListPrivateImpl impl);

    @CoberturaIgnore
    @Binds
    abstract ReadFromPrivate readFromPrivate(ReadFromPrivateImpl impl);

    @CoberturaIgnore
    @Binds
    abstract WriteToPrivate writeToPrivate(WriteToPrivateImpl impl);

    @CoberturaIgnore
    @Binds
    abstract PrivateSpaceService privateSpaceService(PrivateSpaceServiceImpl impl);

    @CoberturaIgnore
    @Binds
    abstract RemoveFromPrivate removeFromPrivate(RemoveFromPrivateImpl impl);
}
