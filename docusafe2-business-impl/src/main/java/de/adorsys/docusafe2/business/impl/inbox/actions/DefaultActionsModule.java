package de.adorsys.docusafe2.business.impl.inbox.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.docusafe2.business.api.inbox.actions.ListInbox;
import de.adorsys.docusafe2.business.api.inbox.actions.ReadFromInbox;
import de.adorsys.docusafe2.business.api.inbox.actions.WriteToInbox;

@Module
public abstract class DefaultActionsModule {

    @Binds
    abstract ListInbox listInbox(ListInboxImpl impl);

    @Binds
    abstract ReadFromInbox readInbox(ReadFromInboxImpl impl);

    @Binds
    abstract WriteToInbox writeInbox(WriteToInboxImpl impl);
}
