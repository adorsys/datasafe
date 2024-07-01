package de.adorsys.datasafe.business.impl.inbox.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.inbox.api.InboxService;
import de.adorsys.datasafe.inbox.api.actions.ListInbox;
import de.adorsys.datasafe.inbox.api.actions.ReadFromInbox;
import de.adorsys.datasafe.inbox.api.actions.RemoveFromInbox;
import de.adorsys.datasafe.inbox.api.actions.WriteToInbox;
import de.adorsys.datasafe.inbox.impl.InboxServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.inbox.impl.actions.ListInboxImplRuntimeDelegatable;
import de.adorsys.datasafe.inbox.impl.actions.ReadFromInboxImplRuntimeDelegatable;
import de.adorsys.datasafe.inbox.impl.actions.RemoveFromInboxImplRuntimeDelegatable;
import de.adorsys.datasafe.inbox.impl.actions.WriteToInboxImplRuntimeDelegatable;

/**
 * This module is responsible for providing default actions on INBOX folder. Paths are not encrypted in INBOX.
 */
@Module
public abstract class DefaultInboxActionsModule {

    /**
     * By default, lists files in users' INBOX location on DFS (privatespace access required).
     */
    @Binds
    abstract ListInbox listInbox(ListInboxImplRuntimeDelegatable impl);

    /**
     * By default, reads and decrypts file (using private key) from users' INBOX location on DFS
     * (privatespace access required).
     */
    @Binds
    abstract ReadFromInbox readInbox(ReadFromInboxImplRuntimeDelegatable impl);

    /**
     * By default, writes file into users' INBOX using his public key (no privatespace access required).
     */
    @Binds
    abstract WriteToInbox writeInbox(WriteToInboxImplRuntimeDelegatable impl);

    /**
     * By default, deletes file from users' INBOX location on DFS (privatespace access required).
     */
    @Binds
    abstract RemoveFromInbox removeFromInbox(RemoveFromInboxImplRuntimeDelegatable impl);

    /**
     * Aggregate view of operations that can be done on INBOX.
     */
    @Binds
    abstract InboxService inboxService(InboxServiceImplRuntimeDelegatable impl);
}
