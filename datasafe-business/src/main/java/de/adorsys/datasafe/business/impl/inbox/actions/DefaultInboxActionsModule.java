package de.adorsys.datasafe.business.impl.inbox.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.inbox.actions.*;

/**
 * This module is responsible for providing default actions on INBOX folder.
 */
@Module
public abstract class DefaultInboxActionsModule {

    @Binds
    abstract ListInbox listInbox(ListInboxImpl impl);

    @Binds
    abstract ReadFromInbox readInbox(ReadFromInboxImpl impl);

    @Binds
    abstract WriteToInbox writeInbox(WriteToInboxImpl impl);

    @Binds
    abstract RemoveFromInbox removeFromInbox(RemoveFromInboxImpl impl);
}
