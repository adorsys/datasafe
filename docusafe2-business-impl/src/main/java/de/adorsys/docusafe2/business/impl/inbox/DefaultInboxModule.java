package de.adorsys.docusafe2.business.impl.inbox;

import dagger.Module;
import dagger.Provides;
import de.adorsys.docusafe2.business.impl.inbox.actions.ListInbox;
import de.adorsys.docusafe2.business.impl.inbox.actions.ReadDocumentFromInbox;
import de.adorsys.docusafe2.business.impl.inbox.actions.WriteDocumentToInbox;

@Module
public class DefaultInboxModule {

    @Provides
    ListInbox listInbox() {
        return new ListInbox();
    }

    @Provides
    ReadDocumentFromInbox readDocumentFromInbox() {
        return new ReadDocumentFromInbox();
    }

    @Provides
    WriteDocumentToInbox writeDocumentToInbox() {
        return new WriteDocumentToInbox();
    }
}
