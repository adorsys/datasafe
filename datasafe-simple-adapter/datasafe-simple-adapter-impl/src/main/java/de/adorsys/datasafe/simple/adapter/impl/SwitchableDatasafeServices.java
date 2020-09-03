package de.adorsys.datasafe.simple.adapter.impl;

import dagger.BindsInstance;
import dagger.Component;
import de.adorsys.datasafe.business.impl.directory.DefaultCredentialsModule;
import de.adorsys.datasafe.business.impl.document.DefaultDocumentModule;
import de.adorsys.datasafe.business.impl.inbox.actions.DefaultInboxActionsModule;
import de.adorsys.datasafe.business.impl.keystore.DefaultKeyStoreModule;
import de.adorsys.datasafe.business.impl.pathencryption.DefaultPathEncryptionModule;
import de.adorsys.datasafe.business.impl.privatestore.actions.DefaultPrivateActionsModule;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.storage.DefaultStorageModule;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.simple.adapter.impl.cmsencryption.SwitchableCMSEncryptionModule;
import de.adorsys.datasafe.simple.adapter.impl.profile.HardcodedProfileModule;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;

import javax.annotation.Nullable;
import javax.inject.Singleton;

@Singleton
@Component(modules = {
        DefaultCredentialsModule.class,
        DefaultKeyStoreModule.class,
        DefaultDocumentModule.class,
        SwitchableCMSEncryptionModule.class,
        DefaultPathEncryptionModule.class,
        DefaultInboxActionsModule.class,
        DefaultPrivateActionsModule.class,
        HardcodedProfileModule.class,
        DefaultStorageModule.class
})
public interface SwitchableDatasafeServices extends DefaultDatasafeServices {


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
         * Provides class overriding functionality, so that you can disable i.e. path encryption
         * @param overridesRegistry Map with class-overrides (note: you can override classes that are
         * annotated with {@code RuntimeDelegate})
         */
        @BindsInstance
        Builder overridesRegistry(@Nullable OverridesRegistry overridesRegistry);

        @BindsInstance
        Builder encryption(@Nullable EncryptionConfig encryptionConfig);
        /**
         * @return Provide NEW instance of <b>Legacy Datasafe</b> services. All dependencies except
         * annotated with {@code @Singleton} will have scope analogous to Spring {code @Prototype}.
         */
        SwitchableDatasafeServices build();
    }
}
