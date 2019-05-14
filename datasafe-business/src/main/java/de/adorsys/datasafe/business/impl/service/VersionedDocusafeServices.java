package de.adorsys.datasafe.business.impl.service;

import dagger.BindsInstance;
import dagger.Component;
import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.api.inbox.InboxServiceImpl;
import de.adorsys.datasafe.business.api.storage.actions.*;
import de.adorsys.datasafe.business.impl.cmsencryption.DefaultCMSEncryptionModule;
import de.adorsys.datasafe.business.impl.directory.DefaultCredentialsModule;
import de.adorsys.datasafe.business.impl.directory.DefaultProfileModule;
import de.adorsys.datasafe.business.impl.document.DefaultDocumentModule;
import de.adorsys.datasafe.business.impl.inbox.actions.DefaultInboxActionsModule;
import de.adorsys.datasafe.business.impl.keystore.DefaultKeyStoreModule;
import de.adorsys.datasafe.business.impl.pathencryption.DefaultPathEncryptionModule;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceServiceImpl;
import de.adorsys.datasafe.business.impl.privatestore.actions.DefaultPrivateActionsModule;
import de.adorsys.datasafe.business.impl.privatestore.actions.DefaultVersionedPrivateActionsModule;
import de.adorsys.datasafe.business.impl.profile.operations.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.business.impl.version.latest.LatestPrivateSpaceImpl;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;

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
        DefaultVersionedPrivateActionsModule.class,
        DefaultProfileModule.class
})
public interface VersionedDocusafeServices extends DefaultDatasafeServices {

    PrivateSpaceServiceImpl privateService();
    LatestPrivateSpaceImpl<LatestDFSVersion> versionedPrivate();
    InboxServiceImpl inboxService();
    DFSBasedProfileStorageImpl userProfile();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder config(DFSConfig config);

        @BindsInstance
        Builder storageList(StorageListService listService);

        @BindsInstance
        Builder storageRead(StorageReadService readService);

        @BindsInstance
        Builder storageWrite(StorageWriteService writeService);

        @BindsInstance
        Builder storageRemove(StorageRemoveService removeService);

        @BindsInstance
        Builder storageCheck(StorageCheckService checkService);

        VersionedDocusafeServices build();
    }
}
