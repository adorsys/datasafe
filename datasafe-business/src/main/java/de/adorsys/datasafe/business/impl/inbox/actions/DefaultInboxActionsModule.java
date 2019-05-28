package de.adorsys.datasafe.business.impl.inbox.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.inbox.api.actions.*;
import de.adorsys.datasafe.inbox.impl.actions.ListInboxImpl;
import de.adorsys.datasafe.inbox.impl.actions.ReadFromInboxImpl;
import de.adorsys.datasafe.inbox.impl.actions.RemoveFromInboxImpl;
import de.adorsys.datasafe.inbox.impl.actions.WriteToInboxImpl;

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
