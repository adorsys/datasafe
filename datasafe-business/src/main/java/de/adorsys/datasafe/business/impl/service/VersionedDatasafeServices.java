package de.adorsys.datasafe.business.impl.service;

import dagger.BindsInstance;
import dagger.Component;
import de.adorsys.datasafe.business.impl.cmsencryption.DefaultCMSEncryptionModule;
import de.adorsys.datasafe.business.impl.directory.DefaultCredentialsModule;
import de.adorsys.datasafe.business.impl.directory.DefaultProfileModule;
import de.adorsys.datasafe.business.impl.document.DefaultDocumentModule;
import de.adorsys.datasafe.business.impl.inbox.actions.DefaultInboxActionsModule;
import de.adorsys.datasafe.business.impl.keystore.DefaultKeyStoreModule;
import de.adorsys.datasafe.business.impl.pathencryption.DefaultPathEncryptionModule;
import de.adorsys.datasafe.business.impl.privatestore.actions.DefaultPrivateActionsModule;
import de.adorsys.datasafe.business.impl.privatestore.actions.DefaultVersionedPrivateActionsModule;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.inbox.impl.InboxServiceImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.DefaultVersionInfoServiceImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.LatestPrivateSpaceImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.types.LatestDFSVersion;
import de.adorsys.datasafe.privatestore.impl.PrivateSpaceServiceImpl;
import de.adorsys.datasafe.storage.api.actions.*;

import javax.inject.Singleton;

/**
 * This is Datasafe services that always work with latest file version using `software` versioning.
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
public interface VersionedDatasafeServices {

    /**
     * @return Provides version information for a given resource (shows only versioned resources)
     */
    DefaultVersionInfoServiceImpl versionInfo();

    /**
     * @return Filtered view of user's private space, that shows only latest files (works only with versioned resources)
     */
    LatestPrivateSpaceImpl<LatestDFSVersion> latestPrivate();

    /**
     * @return Raw view of private user space (shows everything - all versioned and not versioned)
     */
    PrivateSpaceServiceImpl privateService();

    /**
     * @return Inbox service that provides capability to share data between users
     */
    InboxServiceImpl inboxService();

    /**
     * @return User-profile service that provides information necessary for locating his data.
     */
    DFSBasedProfileStorageImpl userProfile();

    /**
     * Binds DFS connection (for example filesystem, minio) and system storage and access
     */
    @Component.Builder
    interface Builder {

        /**
         * Binds (configures) system root uri and system keystore password.
         */
        @BindsInstance
        Builder config(DFSConfig config);

        /**
         * Binds (configures) storage list operation.
         */
        @BindsInstance
        Builder storageList(StorageListService listService);

        /**
         * Binds (configures) storage read operation.
         */
        @BindsInstance
        Builder storageRead(StorageReadService readService);

        /**
         * Binds (configures) storage write operation.
         */
        @BindsInstance
        Builder storageWrite(StorageWriteService writeService);

        /**
         * Binds (configures) storage remove operation.
         */
        @BindsInstance
        Builder storageRemove(StorageRemoveService removeService);

        /**
         * Binds (configures) storage check operation.
         */
        @BindsInstance
        Builder storageCheck(StorageCheckService checkService);

        /**
         * @return Software-versioned Datasafe services.
         */
        VersionedDatasafeServices build();
    }
}
