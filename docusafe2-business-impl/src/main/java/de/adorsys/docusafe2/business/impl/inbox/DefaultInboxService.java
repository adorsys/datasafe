package de.adorsys.docusafe2.business.impl.inbox;

import dagger.Component;
import de.adorsys.docusafe2.business.impl.inbox.impl.InboxServiceImpl;

import javax.inject.Singleton;

@Singleton
@Component(modules = DefaultInboxModule.class)
public interface DefaultInboxService {

    InboxServiceImpl inboxService();
}
