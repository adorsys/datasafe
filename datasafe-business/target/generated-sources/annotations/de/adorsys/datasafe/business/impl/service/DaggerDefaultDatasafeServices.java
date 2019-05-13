package de.adorsys.datasafe.business.impl.service;

import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.api.inbox.InboxServiceImpl;
import de.adorsys.datasafe.business.api.inbox.actions.ListInboxImpl;
import de.adorsys.datasafe.business.api.inbox.actions.ReadFromInboxImpl;
import de.adorsys.datasafe.business.api.inbox.actions.WriteToInboxImpl;
import de.adorsys.datasafe.business.api.storage.StorageListService;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.storage.StorageRemoveService;
import de.adorsys.datasafe.business.api.storage.StorageWriteService;
import de.adorsys.datasafe.business.impl.directory.DefaultCredentialsModule_KeyStoreCacheFactory;
import de.adorsys.datasafe.business.impl.directory.DefaultProfileModule_UserProfileCacheFactory;
import de.adorsys.datasafe.business.impl.encryption.cmsencryption.CMSEncryptionServiceImpl;
import de.adorsys.datasafe.business.impl.encryption.cmsencryption.DefaultCMSEncryptionConfig;
import de.adorsys.datasafe.business.impl.encryption.document.CMSDocumentReadService;
import de.adorsys.datasafe.business.impl.encryption.document.CMSDocumentWriteService;
import de.adorsys.datasafe.business.impl.encryption.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.DefaultPathEncryption;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.encryption.SymmetricPathEncryptionServiceImpl;
import de.adorsys.datasafe.business.impl.pathencryption.DefaultPathEncryptionModule_DigestConfigFactory;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceServiceImpl;
import de.adorsys.datasafe.business.impl.privatespace.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.business.impl.privatespace.actions.ListPrivateImpl;
import de.adorsys.datasafe.business.impl.privatespace.actions.ReadFromPrivateImpl;
import de.adorsys.datasafe.business.impl.privatespace.actions.WriteToPrivateImpl;
import de.adorsys.datasafe.business.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.business.impl.profile.keys.DFSPrivateKeyServiceImpl;
import de.adorsys.datasafe.business.impl.profile.keys.DFSPrivateKeyServiceImpl_Factory;
import de.adorsys.datasafe.business.impl.profile.keys.DFSPublicKeyServiceImpl;
import de.adorsys.datasafe.business.impl.profile.keys.DFSPublicKeyServiceImpl_Factory;
import de.adorsys.datasafe.business.impl.profile.keys.KeyStoreCache;
import de.adorsys.datasafe.business.impl.profile.keys.StreamReadUtil_Factory;
import de.adorsys.datasafe.business.impl.profile.operations.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.business.impl.profile.operations.DFSSystem;
import de.adorsys.datasafe.business.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.business.impl.profile.resource.ResourceResolverImpl;
import de.adorsys.datasafe.business.impl.profile.serde.GsonSerde;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class DaggerDefaultDatasafeServices implements DefaultDatasafeServices {
  private DFSConfig config;

  private StorageReadService storageRead;

  private StorageListService storageList;

  private StorageWriteService storageWrite;

  private StorageRemoveService storageRemove;

  private Provider<KeyStoreCache> keyStoreCacheProvider;

  private Provider<UserProfileCache> userProfileCacheProvider;

  private DaggerDefaultDatasafeServices(Builder builder) {
    initialize(builder);
  }

  public static DefaultDatasafeServices.Builder builder() {
    return new Builder();
  }

  private ResourceResolverImpl getResourceResolverImpl() {
    return new ResourceResolverImpl(userProfile());
  }

  private DefaultPathEncryption getDefaultPathEncryption() {
    return new DefaultPathEncryption(
        DefaultPathEncryptionModule_DigestConfigFactory.proxyDigestConfig());
  }

  private SymmetricPathEncryptionServiceImpl getSymmetricPathEncryptionServiceImpl() {
    return new SymmetricPathEncryptionServiceImpl(getDefaultPathEncryption());
  }

  private DFSSystem getDFSSystem() {
    return new DFSSystem(config);
  }

  private DFSPrivateKeyServiceImpl getDFSPrivateKeyServiceImpl() {
    return DFSPrivateKeyServiceImpl_Factory.newDFSPrivateKeyServiceImpl(
        keyStoreCacheProvider.get(),
        new KeyStoreServiceImpl(),
        getDFSSystem(),
        new BucketAccessServiceImpl(),
        userProfile(),
        StreamReadUtil_Factory.newStreamReadUtil(),
        storageRead);
  }

  private PathEncryptionImpl getPathEncryptionImpl() {
    return new PathEncryptionImpl(
        getSymmetricPathEncryptionServiceImpl(), getDFSPrivateKeyServiceImpl());
  }

  private EncryptedResourceResolver getEncryptedResourceResolver() {
    return new EncryptedResourceResolver(getResourceResolverImpl(), getPathEncryptionImpl());
  }

  private ListPrivateImpl getListPrivateImpl() {
    return new ListPrivateImpl(getEncryptedResourceResolver(), storageList);
  }

  private CMSEncryptionServiceImpl getCMSEncryptionServiceImpl() {
    return new CMSEncryptionServiceImpl(new DefaultCMSEncryptionConfig());
  }

  private CMSDocumentReadService getCMSDocumentReadService() {
    return new CMSDocumentReadService(
        storageRead, getDFSPrivateKeyServiceImpl(), getCMSEncryptionServiceImpl());
  }

  private ReadFromPrivateImpl getReadFromPrivateImpl() {
    return new ReadFromPrivateImpl(getEncryptedResourceResolver(), getCMSDocumentReadService());
  }

  private CMSDocumentWriteService getCMSDocumentWriteService() {
    return new CMSDocumentWriteService(storageWrite, getCMSEncryptionServiceImpl());
  }

  private WriteToPrivateImpl getWriteToPrivateImpl() {
    return new WriteToPrivateImpl(
        getDFSPrivateKeyServiceImpl(),
        getEncryptedResourceResolver(),
        getCMSDocumentWriteService());
  }

  private ListInboxImpl getListInboxImpl() {
    return new ListInboxImpl(getResourceResolverImpl(), storageList);
  }

  private ReadFromInboxImpl getReadFromInboxImpl() {
    return new ReadFromInboxImpl(getResourceResolverImpl(), getCMSDocumentReadService());
  }

  private DFSPublicKeyServiceImpl getDFSPublicKeyServiceImpl() {
    return DFSPublicKeyServiceImpl_Factory.newDFSPublicKeyServiceImpl(
        keyStoreCacheProvider.get(),
        getDFSSystem(),
        new KeyStoreServiceImpl(),
        new BucketAccessServiceImpl(),
        userProfile(),
        StreamReadUtil_Factory.newStreamReadUtil(),
        storageRead);
  }

  private WriteToInboxImpl getWriteToInboxImpl() {
    return new WriteToInboxImpl(
        getDFSPublicKeyServiceImpl(), getResourceResolverImpl(), getCMSDocumentWriteService());
  }

  @SuppressWarnings("unchecked")
  private void initialize(final Builder builder) {
    this.keyStoreCacheProvider =
        DoubleCheck.provider(DefaultCredentialsModule_KeyStoreCacheFactory.create());
    this.config = builder.config;
    this.storageRead = builder.storageRead;
    this.storageList = builder.storageList;
    this.storageWrite = builder.storageWrite;
    this.storageRemove = builder.storageRemove;
    this.userProfileCacheProvider =
        DoubleCheck.provider(DefaultProfileModule_UserProfileCacheFactory.create());
  }

  @Override
  public PrivateSpaceServiceImpl privateService() {
    return new PrivateSpaceServiceImpl(
        getListPrivateImpl(), getReadFromPrivateImpl(), getWriteToPrivateImpl());
  }

  @Override
  public InboxServiceImpl inboxService() {
    return new InboxServiceImpl(getListInboxImpl(), getReadFromInboxImpl(), getWriteToInboxImpl());
  }

  @Override
  public DFSBasedProfileStorageImpl userProfile() {
    return new DFSBasedProfileStorageImpl(
        storageRead,
        storageWrite,
        storageRemove,
        storageList,
        new KeyStoreServiceImpl(),
        getDFSSystem(),
        new GsonSerde(),
        userProfileCacheProvider.get());
  }

  private static final class Builder implements DefaultDatasafeServices.Builder {
    private DFSConfig config;

    private StorageListService storageList;

    private StorageReadService storageRead;

    private StorageWriteService storageWrite;

    private StorageRemoveService storageRemove;

    @Override
    public DefaultDatasafeServices build() {
      if (config == null) {
        throw new IllegalStateException(DFSConfig.class.getCanonicalName() + " must be set");
      }
      if (storageList == null) {
        throw new IllegalStateException(
            StorageListService.class.getCanonicalName() + " must be set");
      }
      if (storageRead == null) {
        throw new IllegalStateException(
            StorageReadService.class.getCanonicalName() + " must be set");
      }
      if (storageWrite == null) {
        throw new IllegalStateException(
            StorageWriteService.class.getCanonicalName() + " must be set");
      }
      if (storageRemove == null) {
        throw new IllegalStateException(
            StorageRemoveService.class.getCanonicalName() + " must be set");
      }
      return new DaggerDefaultDatasafeServices(this);
    }

    @Override
    public Builder config(DFSConfig config) {
      this.config = Preconditions.checkNotNull(config);
      return this;
    }

    @Override
    public Builder storageList(StorageListService listService) {
      this.storageList = Preconditions.checkNotNull(listService);
      return this;
    }

    @Override
    public Builder storageRead(StorageReadService readService) {
      this.storageRead = Preconditions.checkNotNull(readService);
      return this;
    }

    @Override
    public Builder storageWrite(StorageWriteService writeService) {
      this.storageWrite = Preconditions.checkNotNull(writeService);
      return this;
    }

    @Override
    public Builder storageRemove(StorageRemoveService removeService) {
      this.storageRemove = Preconditions.checkNotNull(removeService);
      return this;
    }
  }
}
