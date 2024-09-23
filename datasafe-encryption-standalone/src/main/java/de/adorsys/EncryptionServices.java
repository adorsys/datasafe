package de.adorsys;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dagger.internal.DelegateFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.Provider;
import de.adorsys.config.Config;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.keys.*;
import de.adorsys.datasafe.directory.impl.profile.operations.DefaultUserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRetrievalServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.encryption.CmsEncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.ASNCmsEncryptionConfig;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionServiceImpl;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentReadService;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentWriteService;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.encrypiton.impl.keystore.PublicKeySerdeImpl;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.keymanagement.api.Juggler;
import de.adorsys.keymanagement.api.config.keystore.KeyStoreConfig;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;

import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class EncryptionServices {
    private EncryptionServices() {};
//    private final DFSConfig config;
//    private final StorageService storage;
//    private final Config encryption;

    public static EncryptionServices.EncryptionBuilder builder() {
        return new EncryptionBuilder();
    }

//    private static EncryptionServicesImpl.Builder

//    public EncryptionServices(EncryptionBuilder builder){
//        this.config = builder.config;
//        this.storage = builder.storage;
//        this.encryption = builder.encryption;
//    }
    public static class EncryptionBuilder {
        private DFSConfig config;
        private StorageService storage;
        private EncryptionConfig encryption;
        public static EncryptionBuilder newInstance(){
            return new EncryptionBuilder();
        }
        private EncryptionBuilder() {};

        public EncryptionBuilder setDFSConfig(DFSConfig config) {
            this.config = config;
            return this;
        }
        public EncryptionBuilder setStorage(StorageService storage) {
            this.storage = storage;
            return this;
        }

        public EncryptionBuilder setEncryption(EncryptionConfig encryption) {
            this.encryption = encryption;
            return this;
        }

        public EncryptionServicesImpl build() {
            return new EncryptionServicesImpl(config, storage, encryption);
        }
    }

    public static final class EncryptionServicesImpl{
        private DFSConfig config;

        private StorageService storage;

        private EncryptionConfig encryption;


        public EncryptionServicesImpl(DFSConfig config, StorageService storage, EncryptionConfig encryption) {
            this.config = config;
            this.storage = storage;
            this.encryption = encryption;
        }
        private CmsEncryptionConfig cmsEncryptionConfig(){
            return CmsEncryptionConfig.builder().algo("AES256_GCM").build();
        }
        private ASNCmsEncryptionConfig encryptionConfig(){
            return new ASNCmsEncryptionConfig(cmsEncryptionConfig());
        }
        private CMSEncryptionServiceImpl cmsEncryptionService(){
            return new CMSEncryptionServiceImpl(encryptionConfig());
        }
        private Juggler juggler(){
            return DaggerBCJuggler.builder().keyStoreConfig(keyStoreConfig()).build();
        }
        private KeyStoreConfig keyStoreConfig(){
            return KeyStoreConfig.builder().type("AES256_GCM").build();
        }
        private KeyCreationConfig keyCreationConfig(){
            return KeyCreationConfig.builder().signKeyNumber(1).encKeyNumber(0).build();
        }
        private static KeyStoreCache keyStoreCache(){
            Supplier<Cache<UserID, KeyStore>> cacheKeystore = () -> CacheBuilder.newBuilder()
                    .initialCapacity(1000)
                    // for this interval removed storage access key/changed keystore might not be seen
                    .expireAfterWrite(15, TimeUnit.MINUTES)
                    .build();
            // These are actually static, so we can afford longer expiry time
            Supplier<Cache<UserID, List<PublicKeyIDWithPublicKey>>> cachePubKeys = () -> CacheBuilder.newBuilder()
                    .initialCapacity(1000)
                    .expireAfterWrite(60, TimeUnit.MINUTES)
                    .build();
            return new DefaultKeyStoreCache(
                    cachePubKeys.get().asMap(),
                    cacheKeystore.get().asMap(),
                    // it will generate new instance here
                    cacheKeystore.get().asMap()
            );
        }
        private KeyStoreServiceImpl keyStoreServiceImpl(){
            return new KeyStoreServiceImpl(keyStoreConfig(), juggler());
        }
        private GenericKeystoreOperations genericKeystoreOperations(){
            return new GenericKeystoreOperations(keyCreationConfig(), config, storage, storage, keyStoreCache(), keyStoreServiceImpl());
        }
        private Provider<StorageKeyStoreOperations> storageKeyStoreOperations() {
            return new DelegateFactory<>();
        }
        private BucketAccessServiceImpl bucketAccessServiceImpl(){
            return new BucketAccessServiceImpl( DoubleCheck.lazy(((Provider) storageKeyStoreOperations())));
        }
        private PublicKeySerdeImpl publicKeySerdeImpl(){
            return new PublicKeySerdeImpl();
        }
        private GsonSerde gsonSerde() {return new GsonSerde(publicKeySerdeImpl());}
        private UserProfileCache userProfileCache(){
            Cache<UserID, UserPublicProfile> publicProfileCache = CacheBuilder.newBuilder()
                    .initialCapacity(1000)
                    .expireAfterWrite(15, TimeUnit.MINUTES)
                    .build();

            Cache<UserID, UserPrivateProfile> privateProfileCache = CacheBuilder.newBuilder()
                    .initialCapacity(1000)
                    .expireAfterWrite(15, TimeUnit.MINUTES)
                    .build();

            return new DefaultUserProfileCache(
                    publicProfileCache.asMap(),
                    privateProfileCache.asMap()
            );
        }
        private ProfileRetrievalServiceImpl profileRetrievalServiceImpl(){
            return new ProfileRetrievalServiceImpl(config, storage, storage, bucketAccessServiceImpl(), gsonSerde(), userProfileCache());
        }
        private DocumentKeyStoreOperationsImpl documentKeyStoreOperations(){
            return new DocumentKeyStoreOperationsImpl(keyCreationConfig(),genericKeystoreOperations(), config, bucketAccessServiceImpl(),profileRetrievalServiceImpl(),storage, keyStoreCache(), keyStoreServiceImpl());
        }

        private PrivateKeyService privateKeyService(){
            return new DFSPrivateKeyServiceImpl(documentKeyStoreOperations());
        }
        public DocumentEncryption documentEncryption() {
            return new DocumentEncryption(new CMSDocumentWriteService(storage, cmsEncryptionService()), new CMSDocumentReadService(storage, privateKeyService() ,cmsEncryptionService()));
        }

        public KeyStoreOper keyStoreOper(){
            return new KeyStoreOper();
        }

    }
}
