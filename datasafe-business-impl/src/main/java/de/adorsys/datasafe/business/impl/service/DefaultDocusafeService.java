package de.adorsys.datasafe.business.impl.service;

import dagger.Component;
import de.adorsys.datasafe.business.impl.cmsencryption.DefaultCMSEncryptionModule;
import de.adorsys.datasafe.business.impl.credentials.DefaultCredentialsModule;
import de.adorsys.datasafe.business.impl.dfs.DefaultDFSModule;
import de.adorsys.datasafe.business.impl.document.DefaultDocumentModule;
import de.adorsys.datasafe.business.impl.inbox.actions.DefaultInboxActionsModule;
import de.adorsys.datasafe.business.impl.inbox.impl.InboxServiceImpl;
import de.adorsys.datasafe.business.impl.profile.DefaultProfileModule;
import de.adorsys.datasafe.business.impl.profile.HashMapProfileStorageImpl;

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
public interface DefaultDocusafeService {

    InboxServiceImpl inboxService();
    HashMapProfileStorageImpl userProfile();
}
