package de.adorsys.datasafe.business.impl.inbox.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.inbox.api.actions.ListInbox;
import de.adorsys.datasafe.inbox.api.actions.ReadFromInbox;
import de.adorsys.datasafe.inbox.api.actions.RemoveFromInbox;
import de.adorsys.datasafe.inbox.api.actions.WriteToInbox;
import de.adorsys.datasafe.inbox.impl.actions.ListInboxImpl;
import de.adorsys.datasafe.inbox.impl.actions.ReadFromInboxImpl;
import de.adorsys.datasafe.inbox.impl.actions.RemoveFromInboxImpl;
import de.adorsys.datasafe.inbox.impl.actions.WriteToInboxImpl;

/**
 * This module is responsible for providing default actions on INBOX folder. Paths are not encrypted in INBOX.
 */
@Module
public abstract class DefaultInboxActionsModule {

    /**
     * By default, lists files in users' INBOX location on DFS (privatespace access required).
     */
    @Binds
    abstract ListInbox listInbox(ListInboxImpl impl);

    /**
     * By default, reads and decrypts file (using private key) from users' INBOX location on DFS
     * (privatespace access required).
     */
    @Binds
    abstract ReadFromInbox readInbox(ReadFromInboxImpl impl);

    /**
     * By default, writes file into users' INBOX using his public key (no privatespace access required).
     */
    @Binds
    abstract WriteToInbox writeInbox(WriteToInboxImpl impl);

    /**
     * By default, deletes file from users' INBOX location on DFS (privatespace access required).
     */
    @Binds
    abstract RemoveFromInbox removeFromInbox(RemoveFromInboxImpl impl);
}
