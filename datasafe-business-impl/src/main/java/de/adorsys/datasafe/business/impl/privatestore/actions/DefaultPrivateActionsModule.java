package de.adorsys.datasafe.business.impl.privatestore.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.directory.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.api.directory.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.api.directory.privatespace.actions.WriteToPrivate;

/**
 * This module is responsible for providing default actions on PRIVATE folder.
 */
@Module
public abstract class DefaultPrivateActionsModule {

    @Binds
    abstract ListPrivate listPrivate(ListPrivateImpl impl);

    @Binds
    abstract ReadFromPrivate readFromPrivate(ReadFromPrivateImpl impl);

    @Binds
    abstract WriteToPrivate writeToPrivate(WriteToPrivateImpl impl);
}
