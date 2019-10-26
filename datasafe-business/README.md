# Datasafe business

This module contains ready-to-use Datasafe services and default implementations:
- Default Datasafe service that uses storage adapter to store everything
- Versioned Datasafe service that provides additional safety by storing file versions (software-based)

You can import this module directly or use [interfaces as templates](src/main/java/de/adorsys/datasafe/business/impl/service) 
to build desired services using [Dagger2](https://github.com/google/dagger) compile-time dependency injection.

Also it contains all necessary Dagger-modules with used/provided classes to create Datasafe services. These modules
can be used to build custom services with other-than-default behavior. For example one can construct Datasafe service
that will not encrypt document path.

Simply declare this module:

```java
package de.adorsys.datasafe.business.impl.pathencryption;

import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;

/**
 * This module is responsible for providing No-op path encryption of document.
 */
@Module
public abstract class NoOpPathEncryptionModule {

    @Provides
    public PathEncryption pathEncryption() {
        return new PathEncryption() {
            @Override
            public Uri encrypt(UserIDAuth forUser, Uri path) {
                return path;
            }

            @Override
            public Uri decrypt(UserIDAuth forUser, Uri path) {
                return path;
            }
        };
    }
}
```

And create DatasafeService that has PathEncryptionModule overridden with NoOpPathEncryptionModule:

```java
package de.adorsys.datasafe.business.impl.service;

import dagger.BindsInstance;
import dagger.Component;
import de.adorsys.datasafe.business.impl.cmsencryption.DefaultCMSEncryptionModule;
import de.adorsys.datasafe.business.impl.directory.DefaultCredentialsModule;
import de.adorsys.datasafe.business.impl.directory.DefaultProfileModule;
import de.adorsys.datasafe.business.impl.document.DefaultDocumentModule;
import de.adorsys.datasafe.business.impl.inbox.actions.DefaultInboxActionsModule;
import de.adorsys.datasafe.business.impl.keystore.DefaultKeyStoreModule;
import de.adorsys.datasafe.business.impl.pathencryption.NoOpPathEncryptionModule;
import de.adorsys.datasafe.business.impl.privatestore.actions.DefaultPrivateActionsModule;
import de.adorsys.datasafe.business.impl.storage.DefaultStorageModule;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.inbox.impl.InboxServiceImpl;
import de.adorsys.datasafe.privatestore.impl.PrivateSpaceServiceImpl;
import de.adorsys.datasafe.storage.api.StorageService;

import javax.inject.Singleton;

/**
 * This is Datasafe services with NoOp path encryption.
 */
@Singleton
@Component(modules = {
        DefaultCredentialsModule.class,
        DefaultKeyStoreModule.class,
        DefaultDocumentModule.class,
        DefaultCMSEncryptionModule.class,
        NoOpPathEncryptionModule.class,
        DefaultInboxActionsModule.class,
        DefaultPrivateActionsModule.class,
        DefaultProfileModule.class,
        DefaultStorageModule.class
})
public interface DatasafeServicesWithoutPathEncryption {

    /**
     * Services to access users' privatespace.
     */
    PrivateSpaceServiceImpl privateService();

    /**
     * Services to access users' inbox.
     */
    InboxServiceImpl inboxService();

    /**
     * Services to access users' profiles.
     */
    DFSBasedProfileStorageImpl userProfile();

    /**
     * Binds DFS connection (for example filesystem, minio) and system storage and access
     */
    @Component.Builder
    interface Builder {

        /**
         * Binds (configures) system root uri - where user profiles will be located and system
         * access to open (but not to read key) keystore.
         */
        @BindsInstance
        Builder config(DFSConfig config);

        /**
         * Binds (configures) all storage operations - not necessary to call {@code storageList} after.
         */
        @BindsInstance
        Builder storage(StorageService storageService);

        /**
         * @return Standard Datasafe services.
         */
        DefaultDatasafeServices build();
    }
}
```

