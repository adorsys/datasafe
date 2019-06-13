package de.adorsys.datasafe.simple.adapter.impl;

import dagger.internal.DoubleCheck;
import dagger.internal.InstanceFactory;
import dagger.internal.Preconditions;
import de.adorsys.datasafe.business.impl.directory.DefaultCredentialsModule_KeyStoreCacheFactory;
import de.adorsys.datasafe.business.impl.directory.DefaultProfileModule_UserProfileCacheFactory;
import de.adorsys.datasafe.business.impl.pathencryption.DefaultPathEncryptionModule_DigestConfigFactory;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileOperations;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.DFSPrivateKeyServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.DFSPublicKeyServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.KeyStoreCache;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRegistrationServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRemovalServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRetrievalServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.resource.ResourceResolverImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.DefaultCMSEncryptionConfig;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentReadServiceRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentWriteServiceRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.keystore.PublicKeySerdeImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.DefaultPathEncryptionRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.SymmetricPathEncryptionServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.inbox.api.InboxService;
import de.adorsys.datasafe.inbox.impl.InboxServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.inbox.impl.actions.ListInboxImplRuntimeDelegatable;
import de.adorsys.datasafe.inbox.impl.actions.ReadFromInboxImplRuntimeDelegatable;
import de.adorsys.datasafe.inbox.impl.actions.RemoveFromInboxImplRuntimeDelegatable;
import de.adorsys.datasafe.inbox.impl.actions.WriteToInboxImplRuntimeDelegatable;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.privatestore.impl.PrivateSpaceServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.privatestore.impl.actions.*;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Provider;

@Slf4j
public final class DaggerSimpleAdapterDatasafeSerivce implements DefaultDatasafeServices {
    private OverridesRegistry overridesRegistry;

    private DFSConfig config;

    private StorageService storage;

    private Provider<UserProfileCache> userProfileCacheProvider;

    private Provider<OverridesRegistry> overridesRegistryProvider;

    private Provider<KeyStoreCache> keyStoreCacheProvider;

    private static final String NO_BUCKETPATH_ENCRYPTION = "SC-NO-BUCKETPATH-ENCRYPTION";

    private boolean withPathEncryption = true;

    private DaggerSimpleAdapterDatasafeSerivce(DaggerSimpleAdapterDatasafeSerivce.Builder builder) {
        initialize(builder);
        initialize(builder);
        if (System.getProperty(NO_BUCKETPATH_ENCRYPTION) != null) {
            log.info("path encryption is off");
            withPathEncryption = false;
        }
    }

    public static DefaultDatasafeServices.Builder builder() {
        return new DaggerSimpleAdapterDatasafeSerivce.Builder();
    }

    private BucketAccessServiceImplRuntimeDelegatable getBucketAccessServiceImplRuntimeDelegatable() {
        return new BucketAccessServiceImplRuntimeDelegatable(overridesRegistry);
    }

    private PublicKeySerdeImplRuntimeDelegatable getPublicKeySerdeImplRuntimeDelegatable() {
        return new PublicKeySerdeImplRuntimeDelegatable(overridesRegistry);
    }

    private GsonSerde getGsonSerde() {
        return new GsonSerde(getPublicKeySerdeImplRuntimeDelegatable());
    }

    private ProfileRetrievalServiceImplRuntimeDelegatable
    getProfileRetrievalServiceImplRuntimeDelegatable() {
        return new ProfileRetrievalServiceImplRuntimeDelegatable(
                overridesRegistry,
                config,
                storage,
                storage,
                getGsonSerde(),
                userProfileCacheProvider.get());
    }

    private ResourceResolverImplRuntimeDelegatable getResourceResolverImplRuntimeDelegatable() {
        return new ResourceResolverImplRuntimeDelegatable(
                overridesRegistry,
                getProfileRetrievalServiceImplRuntimeDelegatable(),
                getBucketAccessServiceImplRuntimeDelegatable());
    }

    private DefaultPathEncryptionRuntimeDelegatable getDefaultPathEncryptionRuntimeDelegatable() {
        return new DefaultPathEncryptionRuntimeDelegatable(
                overridesRegistry, DefaultPathEncryptionModule_DigestConfigFactory.proxyDigestConfig());
    }

    private SymmetricPathEncryptionServiceImplRuntimeDelegatable
    getSymmetricPathEncryptionServiceImplRuntimeDelegatable() {
        return new SymmetricPathEncryptionServiceImplRuntimeDelegatable(
                overridesRegistry, getDefaultPathEncryptionRuntimeDelegatable());
    }

    private KeyStoreServiceImplRuntimeDelegatable getKeyStoreServiceImplRuntimeDelegatable() {
        return new KeyStoreServiceImplRuntimeDelegatable(overridesRegistry);
    }

    private DFSPrivateKeyServiceImplRuntimeDelegatable
    getDFSPrivateKeyServiceImplRuntimeDelegatable() {
        return new DFSPrivateKeyServiceImplRuntimeDelegatable(
                overridesRegistry,
                keyStoreCacheProvider.get(),
                getKeyStoreServiceImplRuntimeDelegatable(),
                config,
                getBucketAccessServiceImplRuntimeDelegatable(),
                getProfileRetrievalServiceImplRuntimeDelegatable(),
                storage);
    }

    private PathEncryptionImplRuntimeDelegatable getPathEncryptionImplRuntimeDelegatable() {
        if (withPathEncryption) {
            return new PathEncryptionImplRuntimeDelegatable(
                    overridesRegistry,
                    getSymmetricPathEncryptionServiceImplRuntimeDelegatable(),
                    getDFSPrivateKeyServiceImplRuntimeDelegatable());
        }
        return new NoPathEncryptionImpl(overridesRegistry,
                getSymmetricPathEncryptionServiceImplRuntimeDelegatable(),
                getDFSPrivateKeyServiceImplRuntimeDelegatable());
    }

    private EncryptedResourceResolverImplRuntimeDelegatable
    getEncryptedResourceResolverImplRuntimeDelegatable() {
        return new EncryptedResourceResolverImplRuntimeDelegatable(
                overridesRegistry,
                getBucketAccessServiceImplRuntimeDelegatable(),
                getResourceResolverImplRuntimeDelegatable(),
                getPathEncryptionImplRuntimeDelegatable());
    }

    private ListPrivateImplRuntimeDelegatable getListPrivateImplRuntimeDelegatable() {
        return new ListPrivateImplRuntimeDelegatable(
                overridesRegistry, getEncryptedResourceResolverImplRuntimeDelegatable(), storage);
    }

    private CMSEncryptionServiceImplRuntimeDelegatable
    getCMSEncryptionServiceImplRuntimeDelegatable() {
        return new CMSEncryptionServiceImplRuntimeDelegatable(
                overridesRegistry, new DefaultCMSEncryptionConfig());
    }

    private CMSDocumentReadServiceRuntimeDelegatable getCMSDocumentReadServiceRuntimeDelegatable() {
        return new CMSDocumentReadServiceRuntimeDelegatable(
                overridesRegistry,
                storage,
                getDFSPrivateKeyServiceImplRuntimeDelegatable(),
                getCMSEncryptionServiceImplRuntimeDelegatable());
    }

    private ReadFromPrivateImplRuntimeDelegatable getReadFromPrivateImplRuntimeDelegatable() {
        return new ReadFromPrivateImplRuntimeDelegatable(
                overridesRegistry,
                getEncryptedResourceResolverImplRuntimeDelegatable(),
                getCMSDocumentReadServiceRuntimeDelegatable());
    }

    private CMSDocumentWriteServiceRuntimeDelegatable getCMSDocumentWriteServiceRuntimeDelegatable() {
        return new CMSDocumentWriteServiceRuntimeDelegatable(
                overridesRegistry, storage, getCMSEncryptionServiceImplRuntimeDelegatable());
    }

    private WriteToPrivateImplRuntimeDelegatable getWriteToPrivateImplRuntimeDelegatable() {
        return new WriteToPrivateImplRuntimeDelegatable(
                overridesRegistry,
                getDFSPrivateKeyServiceImplRuntimeDelegatable(),
                getEncryptedResourceResolverImplRuntimeDelegatable(),
                getCMSDocumentWriteServiceRuntimeDelegatable());
    }

    private RemoveFromPrivateImplRuntimeDelegatable getRemoveFromPrivateImplRuntimeDelegatable() {
        return new RemoveFromPrivateImplRuntimeDelegatable(
                overridesRegistry, getEncryptedResourceResolverImplRuntimeDelegatable(), storage);
    }

    private PrivateSpaceServiceImplRuntimeDelegatable getPrivateSpaceServiceImplRuntimeDelegatable() {
        return new PrivateSpaceServiceImplRuntimeDelegatable(
                overridesRegistry,
                getListPrivateImplRuntimeDelegatable(),
                getReadFromPrivateImplRuntimeDelegatable(),
                getWriteToPrivateImplRuntimeDelegatable(),
                getRemoveFromPrivateImplRuntimeDelegatable());
    }

    private ListInboxImplRuntimeDelegatable getListInboxImplRuntimeDelegatable() {
        return new ListInboxImplRuntimeDelegatable(
                overridesRegistry,
                getProfileRetrievalServiceImplRuntimeDelegatable(),
                getResourceResolverImplRuntimeDelegatable(),
                storage);
    }

    private ReadFromInboxImplRuntimeDelegatable getReadFromInboxImplRuntimeDelegatable() {
        return new ReadFromInboxImplRuntimeDelegatable(
                overridesRegistry,
                getResourceResolverImplRuntimeDelegatable(),
                getCMSDocumentReadServiceRuntimeDelegatable());
    }

    private DFSPublicKeyServiceImplRuntimeDelegatable getDFSPublicKeyServiceImplRuntimeDelegatable() {
        return new DFSPublicKeyServiceImplRuntimeDelegatable(
                overridesRegistry,
                keyStoreCacheProvider.get(),
                getBucketAccessServiceImplRuntimeDelegatable(),
                getProfileRetrievalServiceImplRuntimeDelegatable(),
                storage,
                getGsonSerde());
    }

    private WriteToInboxImplRuntimeDelegatable getWriteToInboxImplRuntimeDelegatable() {
        return new WriteToInboxImplRuntimeDelegatable(
                overridesRegistry,
                getDFSPublicKeyServiceImplRuntimeDelegatable(),
                getResourceResolverImplRuntimeDelegatable(),
                getCMSDocumentWriteServiceRuntimeDelegatable());
    }

    private RemoveFromInboxImplRuntimeDelegatable getRemoveFromInboxImplRuntimeDelegatable() {
        return new RemoveFromInboxImplRuntimeDelegatable(
                overridesRegistry, getResourceResolverImplRuntimeDelegatable(), storage);
    }

    private InboxServiceImplRuntimeDelegatable getInboxServiceImplRuntimeDelegatable() {
        return new InboxServiceImplRuntimeDelegatable(
                overridesRegistry,
                getListInboxImplRuntimeDelegatable(),
                getReadFromInboxImplRuntimeDelegatable(),
                getWriteToInboxImplRuntimeDelegatable(),
                getRemoveFromInboxImplRuntimeDelegatable());
    }

    private ProfileRegistrationServiceImplRuntimeDelegatable
    getProfileRegistrationServiceImplRuntimeDelegatable() {
        return new ProfileRegistrationServiceImplRuntimeDelegatable(
                overridesRegistry,
                getKeyStoreServiceImplRuntimeDelegatable(),
                storage,
                storage,
                getGsonSerde(),
                config);
    }

    private ProfileRemovalServiceImplRuntimeDelegatable
    getProfileRemovalServiceImplRuntimeDelegatable() {
        return new ProfileRemovalServiceImplRuntimeDelegatable(
                overridesRegistry,
                storage,
                config,
                storage,
                getProfileRetrievalServiceImplRuntimeDelegatable());
    }

    private DFSBasedProfileStorageImplRuntimeDelegatable
    getDFSBasedProfileStorageImplRuntimeDelegatable() {
        return new DFSBasedProfileStorageImplRuntimeDelegatable(
                overridesRegistry,
                getProfileRegistrationServiceImplRuntimeDelegatable(),
                getProfileRetrievalServiceImplRuntimeDelegatable(),
                getProfileRemovalServiceImplRuntimeDelegatable());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final DaggerSimpleAdapterDatasafeSerivce.Builder builder) {
        this.overridesRegistry = builder.overridesRegistry;
        this.config = builder.config;
        this.storage = builder.storage;
        this.userProfileCacheProvider =
                DoubleCheck.provider(DefaultProfileModule_UserProfileCacheFactory.create());
        this.overridesRegistryProvider = InstanceFactory.createNullable(builder.overridesRegistry);
        this.keyStoreCacheProvider =
                DoubleCheck.provider(
                        DefaultCredentialsModule_KeyStoreCacheFactory.create(overridesRegistryProvider));
    }

    @Override
    public PrivateSpaceService privateService() {
        return getPrivateSpaceServiceImplRuntimeDelegatable();
    }

    @Override
    public InboxService inboxService() {
        return getInboxServiceImplRuntimeDelegatable();
    }

    @Override
    public ProfileOperations userProfile() {
        return getDFSBasedProfileStorageImplRuntimeDelegatable();
    }

    private static final class Builder implements DefaultDatasafeServices.Builder {
        private DFSConfig config;

        private StorageService storage;

        private OverridesRegistry overridesRegistry;

        @Override
        public DefaultDatasafeServices build() {
            if (config == null) {
                throw new IllegalStateException(DFSConfig.class.getCanonicalName() + " must be set");
            }
            if (storage == null) {
                throw new IllegalStateException(StorageService.class.getCanonicalName() + " must be set");
            }
            return new DaggerSimpleAdapterDatasafeSerivce(this);
        }

        @Override
        public DaggerSimpleAdapterDatasafeSerivce.Builder config(DFSConfig config) {
            this.config = Preconditions.checkNotNull(config);
            return this;
        }

        @Override
        public DaggerSimpleAdapterDatasafeSerivce.Builder storage(StorageService storageService) {
            this.storage = Preconditions.checkNotNull(storageService);
            return this;
        }

        @Override
        public DaggerSimpleAdapterDatasafeSerivce.Builder overridesRegistry(OverridesRegistry overridesRegistry) {
            this.overridesRegistry = overridesRegistry;
            return this;
        }
    }
}
