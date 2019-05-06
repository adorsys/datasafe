package de.adorsys.datasafe.business.impl.service;

import dagger.BindsInstance;
import dagger.Component;
import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.api.inbox.InboxServiceImpl;
import de.adorsys.datasafe.business.api.storage.StorageListService;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.storage.StorageWriteService;
import de.adorsys.datasafe.business.impl.cmsencryption.DefaultCMSEncryptionModule;
import de.adorsys.datasafe.business.impl.directory.DefaultCredentialsModule;
import de.adorsys.datasafe.business.impl.directory.DefaultProfileModule;
import de.adorsys.datasafe.business.impl.document.DefaultDocumentModule;
import de.adorsys.datasafe.business.impl.inbox.actions.DefaultInboxActionsModule;
import de.adorsys.datasafe.business.impl.keystore.DefaultKeyStoreModule;
import de.adorsys.datasafe.business.impl.pathencryption.DefaultPathEncryptionModule;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceServiceImpl;
import de.adorsys.datasafe.business.impl.privatestore.actions.DefaultPrivateActionsModule;
import de.adorsys.datasafe.business.impl.profile.operations.DFSBasedProfileStorageImpl;

import javax.inject.Singleton;

/**
 * This is user Docusafe services default implementation.
 */
@Singleton
@Component(modules = {
        DefaultCredentialsModule.class,
        DefaultKeyStoreModule.class,
        DefaultDocumentModule.class,
        DefaultCMSEncryptionModule.class,
        DefaultPathEncryptionModule.class,
        DefaultInboxActionsModule.class,
        DefaultPrivateActionsModule.class,
        DefaultProfileModule.class
})
public interface DefaultDocusafeServices {

    PrivateSpaceServiceImpl privateService();
    InboxServiceImpl inboxService();
    DFSBasedProfileStorageImpl userProfile();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder config(DFSConfig config);

        @BindsInstance
        Builder storageList(StorageListService listStorage);

        @BindsInstance
        Builder storageRead(StorageReadService listStorage);

        @BindsInstance
        Builder storageWrite(StorageWriteService listStorage);

        DefaultDocusafeServices build();
    }

}
