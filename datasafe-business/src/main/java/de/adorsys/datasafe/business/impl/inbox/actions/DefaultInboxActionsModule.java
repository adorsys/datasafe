package de.adorsys.datasafe.business.impl.inbox.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.inbox.actions.*;
import de.adorsys.datasafe.business.api.types.cobertura.CoberturaIgnore;

/**
 * This module is responsible for providing default actions on INBOX folder.
 */
@Module
public abstract class DefaultInboxActionsModule {

    @CoberturaIgnore
    @Binds
    abstract ListInbox listInbox(ListInboxImpl impl);

    @CoberturaIgnore
    @Binds
    abstract ReadFromInbox readInbox(ReadFromInboxImpl impl);

    @CoberturaIgnore
    @Binds
    abstract WriteToInbox writeInbox(WriteToInboxImpl impl);

    @CoberturaIgnore
    @Binds
    abstract RemoveFromInbox removeFromInbox(RemoveFromInboxImpl impl);
}
