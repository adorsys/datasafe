package de.adorsys.datasafe.simple.adapter.impl;

import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import de.adorsys.datasafe.business.impl.directory.DefaultCredentialsModule_KeyStoreCacheFactory;
import de.adorsys.datasafe.business.impl.directory.DefaultProfileModule_UserProfileCacheFactory;
import de.adorsys.datasafe.business.impl.pathencryption.DefaultPathEncryptionModule_DigestConfigFactory;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.keys.DFSPrivateKeyServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.keys.DFSPublicKeyServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.keys.KeyStoreCache;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRegistrationServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRemovalServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRetrievalServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.resource.ResourceResolverImpl;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionServiceImpl;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.DefaultCMSEncryptionConfig;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentReadService;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentWriteService;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.encrypiton.impl.keystore.PublicKeySerdeImpl_Factory;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.DefaultPathEncryption;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.SymmetricPathEncryptionServiceImpl;
import de.adorsys.datasafe.inbox.impl.InboxServiceImpl;
import de.adorsys.datasafe.inbox.impl.actions.ListInboxImpl;
import de.adorsys.datasafe.inbox.impl.actions.ReadFromInboxImpl;
import de.adorsys.datasafe.inbox.impl.actions.RemoveFromInboxImpl;
import de.adorsys.datasafe.inbox.impl.actions.WriteToInboxImpl;
import de.adorsys.datasafe.privatestore.impl.PrivateSpaceServiceImpl;
import de.adorsys.datasafe.privatestore.impl.actions.*;
import de.adorsys.datasafe.storage.api.StorageService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Provider;

@Slf4j
public final class DaggerSimpleAdapterDatasafeSerivce implements DefaultDatasafeServices {
    private DFSConfig config;

    private StorageService storage;

    private Provider<UserProfileCache> userProfileCacheProvider;

    private Provider<KeyStoreCache> keyStoreCacheProvider;

    private static final String NO_BUCKETPATH_ENCRYPTION = "SC-NO-BUCKETPATH-ENCRYPTION";

    private boolean withPathEncryption = true;

    private DaggerSimpleAdapterDatasafeSerivce(DaggerSimpleAdapterDatasafeSerivce.Builder builder) {
        initialize(builder);
        if (System.getProperty(NO_BUCKETPATH_ENCRYPTION) != null) {
            log.info("path encryption is off");
            withPathEncryption = false;
        }
    }

    public static DefaultDatasafeServices.Builder builder() {
        return new DaggerSimpleAdapterDatasafeSerivce.Builder();
    }

    private GsonSerde getGsonSerde() {
        return new GsonSerde(PublicKeySerdeImpl_Factory.newPublicKeySerdeImpl());
    }

    private ProfileRetrievalServiceImpl getProfileRetrievalServiceImpl() {
        return new ProfileRetrievalServiceImpl(
                config, storage, storage, getGsonSerde(), userProfileCacheProvider.get());
    }

    private ResourceResolverImpl getResourceResolverImpl() {
        return new ResourceResolverImpl(
                getProfileRetrievalServiceImpl(), new BucketAccessServiceImpl());
    }

    private DefaultPathEncryption getDefaultPathEncryption() {
        return new DefaultPathEncryption(
                DefaultPathEncryptionModule_DigestConfigFactory.proxyDigestConfig());
    }

    private SymmetricPathEncryptionServiceImpl getSymmetricPathEncryptionServiceImpl() {
        return new SymmetricPathEncryptionServiceImpl(getDefaultPathEncryption());
    }

    private DFSPrivateKeyServiceImpl getDFSPrivateKeyServiceImpl() {
        return new DFSPrivateKeyServiceImpl(
                keyStoreCacheProvider.get(),
                new KeyStoreServiceImpl(),
                config,
                new BucketAccessServiceImpl(),
                getProfileRetrievalServiceImpl(),
                storage);
    }

    private PathEncryption getPathEncryption() {
        if (withPathEncryption) {
            return new PathEncryptionImpl(
                    getSymmetricPathEncryptionServiceImpl(), getDFSPrivateKeyServiceImpl());
        }
        return new NoPathEncryptionImpl();
    }

    private EncryptedResourceResolverImpl getEncryptedResourceResolverImpl() {
        return new EncryptedResourceResolverImpl(
                new BucketAccessServiceImpl(), getResourceResolverImpl(), getPathEncryption());
    }

    private ListPrivateImpl getListPrivateImpl() {
        return new ListPrivateImpl(getEncryptedResourceResolverImpl(), storage);
    }

    private CMSEncryptionServiceImpl getCMSEncryptionServiceImpl() {
        return new CMSEncryptionServiceImpl(new DefaultCMSEncryptionConfig());
    }

    private CMSDocumentReadService getCMSDocumentReadService() {
        return new CMSDocumentReadService(
                storage, getDFSPrivateKeyServiceImpl(), getCMSEncryptionServiceImpl());
    }

    private ReadFromPrivateImpl getReadFromPrivateImpl() {
        return new ReadFromPrivateImpl(getEncryptedResourceResolverImpl(), getCMSDocumentReadService());
    }

    private CMSDocumentWriteService getCMSDocumentWriteService() {
        return new CMSDocumentWriteService(storage, getCMSEncryptionServiceImpl());
    }

    private WriteToPrivateImpl getWriteToPrivateImpl() {
        return new WriteToPrivateImpl(
                getDFSPrivateKeyServiceImpl(),
                getEncryptedResourceResolverImpl(),
                getCMSDocumentWriteService());
    }

    private RemoveFromPrivateImpl getRemoveFromPrivateImpl() {
        return new RemoveFromPrivateImpl(getEncryptedResourceResolverImpl(), storage);
    }

    private ListInboxImpl getListInboxImpl() {
        return new ListInboxImpl(getProfileRetrievalServiceImpl(), getResourceResolverImpl(), storage);
    }

    private ReadFromInboxImpl getReadFromInboxImpl() {
        return new ReadFromInboxImpl(getResourceResolverImpl(), getCMSDocumentReadService());
    }

    private DFSPublicKeyServiceImpl getDFSPublicKeyServiceImpl() {
        return new DFSPublicKeyServiceImpl(
                keyStoreCacheProvider.get(),
                new BucketAccessServiceImpl(),
                getProfileRetrievalServiceImpl(),
                storage,
                getGsonSerde());
    }

    private WriteToInboxImpl getWriteToInboxImpl() {
        return new WriteToInboxImpl(
                getDFSPublicKeyServiceImpl(), getResourceResolverImpl(), getCMSDocumentWriteService());
    }

    private RemoveFromInboxImpl getRemoveFromInboxImpl() {
        return new RemoveFromInboxImpl(getResourceResolverImpl(), storage);
    }

    private ProfileRegistrationServiceImpl getProfileRegistrationServiceImpl() {
        return new ProfileRegistrationServiceImpl(
                new KeyStoreServiceImpl(), storage, storage, getGsonSerde(), config);
    }

    private ProfileRemovalServiceImpl getProfileRemovalServiceImpl() {
        return new ProfileRemovalServiceImpl(
                storage, config, storage, getProfileRetrievalServiceImpl());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final DaggerSimpleAdapterDatasafeSerivce.Builder builder) {
        this.config = builder.config;
        this.storage = builder.storage;
        this.userProfileCacheProvider =
                DoubleCheck.provider(DefaultProfileModule_UserProfileCacheFactory.create());
        this.keyStoreCacheProvider =
                DoubleCheck.provider(DefaultCredentialsModule_KeyStoreCacheFactory.create());
    }

    @Override
    public PrivateSpaceServiceImpl privateService() {
        return new PrivateSpaceServiceImpl(
                getListPrivateImpl(),
                getReadFromPrivateImpl(),
                getWriteToPrivateImpl(),
                getRemoveFromPrivateImpl());
    }

    @Override
    public InboxServiceImpl inboxService() {
        return new InboxServiceImpl(
                getListInboxImpl(),
                getReadFromInboxImpl(),
                getWriteToInboxImpl(),
                getRemoveFromInboxImpl());
    }

    @Override
    public DFSBasedProfileStorageImpl userProfile() {
        return new DFSBasedProfileStorageImpl(
                getProfileRegistrationServiceImpl(),
                getProfileRetrievalServiceImpl(),
                getProfileRemovalServiceImpl());
    }

    private static final class Builder implements DefaultDatasafeServices.Builder {
        private DFSConfig config;

        private StorageService storage;

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
    }
}
