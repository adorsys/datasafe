package de.adorsys.docusafe2.business.impl.inbox;

import dagger.Component;
import de.adorsys.docusafe2.business.impl.cmsencryption.DefaultCMSEncryptionModule;
import de.adorsys.docusafe2.business.impl.credentials.DefaultCredentialsModule;
import de.adorsys.docusafe2.business.impl.dfs.DefaultDFSModule;
import de.adorsys.docusafe2.business.impl.document.DefaultDocumentModule;
import de.adorsys.docusafe2.business.impl.inbox.actions.DefaultInboxActionsModule;
import de.adorsys.docusafe2.business.impl.inbox.impl.InboxServiceImpl;
import de.adorsys.docusafe2.business.impl.profile.DefaultProfileModule;

import javax.inject.Singleton;

/**
 * This is user INBOX service default implementation.
 */
@Singleton
@Component(modules = {
        DefaultCredentialsModule.class,
        DefaultDocumentModule.class,
        DefaultDFSModule.class,
        DefaultCMSEncryptionModule.class,
        DefaultInboxActionsModule.class,
        DefaultProfileModule.class
})
public interface DefaultInboxService {

    InboxServiceImpl inboxService();
}
