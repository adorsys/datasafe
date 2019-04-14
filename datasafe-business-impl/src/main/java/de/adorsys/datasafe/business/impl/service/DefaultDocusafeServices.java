package de.adorsys.datasafe.business.impl.service;

import dagger.Component;
import de.adorsys.datasafe.business.impl.cmsencryption.DefaultCMSEncryptionModule;
import de.adorsys.datasafe.business.impl.credentials.DefaultCredentialsModule;
import de.adorsys.datasafe.business.impl.dfs.DefaultDFSModule;
import de.adorsys.datasafe.business.impl.document.DefaultDocumentModule;
import de.adorsys.datasafe.business.impl.inbox.actions.DefaultInboxActionsModule;
import de.adorsys.datasafe.business.impl.impl.InboxServiceImpl;
import de.adorsys.datasafe.business.impl.keystore.DefaultKeyStoreModule;
import de.adorsys.datasafe.business.impl.privatestore.actions.DefaultPrivateActionsModule;
import de.adorsys.datasafe.business.impl.privatestore.impl.PrivateSpaceServiceImpl;
import de.adorsys.datasafe.business.impl.profile.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.business.impl.profile.DefaultProfileModule;

import javax.inject.Singleton;

/**
 * This is user INBOX service default implementation.
 */
@Singleton
@Component(modules = {
        DefaultCredentialsModule.class,
        DefaultKeyStoreModule.class,
        DefaultDocumentModule.class,
        DefaultDFSModule.class,
        DefaultCMSEncryptionModule.class,
        DefaultInboxActionsModule.class,
        DefaultPrivateActionsModule.class,
        DefaultProfileModule.class
})
public interface DefaultDocusafeServices {

    PrivateSpaceServiceImpl privateService();
    InboxServiceImpl inboxService();
    DFSBasedProfileStorageImpl userProfile();
}
