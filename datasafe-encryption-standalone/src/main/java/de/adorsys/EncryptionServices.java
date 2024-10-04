package de.adorsys;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dagger.internal.DelegateFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.Provider;
import de.adorsys.config.Config;
import de.adorsys.config.Properties;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.*;
import de.adorsys.datasafe.directory.impl.profile.operations.DefaultUserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRetrievalServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRetrievalServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileStoreService;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.CmsEncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.ASNCmsEncryptionConfig;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionServiceImpl;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentReadService;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentReadServiceRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentWriteService;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentWriteServiceRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.keystore.PublicKeySerdeImpl;
import de.adorsys.datasafe.encrypiton.impl.keystore.PublicKeySerdeImplRuntimeDelegatable;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
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
    public static EncryptionBuilder builder() {
        return new EncryptionBuilder();
    }

    public static class EncryptionBuilder {
        private DFSConfig config;
        private StorageService storage;
        private EncryptionConfig encryption;
        private OverridesRegistry overridesRegistry;
        private int algorithm;
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
        public EncryptionBuilder setOverridesRegistry(OverridesRegistry overridesRegistry) {
            this.overridesRegistry = overridesRegistry;
            return this;
        }
        public EncryptionBuilder setAlgorithm(int algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public EncryptionServicesImpl build() {
            return new EncryptionServicesImpl(config, storage, encryption, overridesRegistry, algorithm);
        }
    }

    public static final class EncryptionServicesImpl{
        private DFSConfig config;

        private StorageService storage;

        private EncryptionConfig encryption;
        private OverridesRegistry overridesRegistry;
        private int algorithm;

        public EncryptionServicesImpl(DFSConfig config, StorageService storage, EncryptionConfig encryption, OverridesRegistry overridesRegistry, int algorithm) {
            this.config = config;
            this.storage = storage;
            this.encryption = encryption;
            this.overridesRegistry = overridesRegistry;
            this.algorithm = algorithm;
        }
        private void init(){
            // initialize services here
        }
        private CmsEncryptionConfig cmsEncryptionConfig(){
            if (null == encryption) {
                return EncryptionConfig.builder().build().getCms();
            }
            return encryption.getCms();
        }
        private ASNCmsEncryptionConfig encryptionConfig(){
            return new ASNCmsEncryptionConfig(cmsEncryptionConfig());
        }
        private CMSEncryptionServiceImplRuntimeDelegatable cmsEncryptionServiceImplRuntimeDelegatable(){
            return new CMSEncryptionServiceImplRuntimeDelegatable(overridesRegistry, encryptionConfig());
        }
        private Juggler juggler(){
            return DaggerBCJuggler.builder().keyStoreConfig(keyStoreConfig()).build();
        }
        private KeyStoreConfig keyStoreConfig(){
            if(null == encryption){
              return EncryptionConfig.builder().build().getKeystore();
            }
            return encryption.getKeystore();
        }
        private KeyCreationConfig keyCreationConfig(){
            if(null == encryption){
                return EncryptionConfig.builder().build().getKeys();
            }
            return getCustomKeyCreationConfig(algorithm);
        }
        private KeyCreationConfig getCustomKeyCreationConfig(int Algorithm) {
            if(Algorithm == 1) {
                return KeyCreationConfig.builder()
                        .signing(
                                KeyCreationConfig.SigningKeyCreationCfg.builder().algo("RSA").size(4096).sigAlgo( "SHA256withRSA").curve(null).build())
                        .encrypting(
                                KeyCreationConfig.EncryptingKeyCreationCfg.builder().algo("RSA").
                                size(4096).sigAlgo("SHA256withRSA").curve(null).build())
                        .build();
            } else {
                return encryption.getKeys();
            }


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
        private KeyStoreServiceImplRuntimeDelegatable keyStoreServiceImplRuntimeDelegatable(){
            return new KeyStoreServiceImplRuntimeDelegatable(overridesRegistry, keyStoreConfig(), juggler());
        }
        private GenericKeystoreOperations genericKeystoreOperations(){
            return new GenericKeystoreOperations(keyCreationConfig(), config, storage, storage, keyStoreCache(), keyStoreServiceImplRuntimeDelegatable());
        }
        private Provider<StorageKeyStoreOperationsImplRuntimeDelegatable> storageKeyStoreOperationsImplRuntimeDelegatableProvider() {
            return new DelegateFactory<>();
        }
        private BucketAccessServiceImplRuntimeDelegatable bucketAccessServiceImplRuntimeDelegatable(){
            return new BucketAccessServiceImplRuntimeDelegatable(overridesRegistry, DoubleCheck.lazy(((Provider) storageKeyStoreOperationsImplRuntimeDelegatableProvider())));
        }
        private PublicKeySerdeImplRuntimeDelegatable publicKeySerdeImplRuntimeDelegatable(){
            return new PublicKeySerdeImplRuntimeDelegatable(overridesRegistry);
        }
        private GsonSerde gsonSerde() {return new GsonSerde(publicKeySerdeImplRuntimeDelegatable());}
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
        private ProfileRetrievalServiceImplRuntimeDelegatable profileRetrievalServiceImplRuntimeDelegatable(){
            return new ProfileRetrievalServiceImplRuntimeDelegatable(overridesRegistry, config, storage, storage, bucketAccessServiceImplRuntimeDelegatable(), gsonSerde(), userProfileCache());
        }
        private DocumentKeyStoreOperationsImplRuntimeDelegatable documentKeyStoreOperationsImplRuntimeDelegatable(){
            return new DocumentKeyStoreOperationsImplRuntimeDelegatable(overridesRegistry, keyCreationConfig(),genericKeystoreOperations(), config, bucketAccessServiceImplRuntimeDelegatable(),
                    profileRetrievalServiceImplRuntimeDelegatable(), storage, keyStoreCache(), keyStoreServiceImplRuntimeDelegatable());
        }

        private DFSPrivateKeyServiceImplRuntimeDelegatable privateKeyServiceImplRuntimeDelegatable(){
            return new DFSPrivateKeyServiceImplRuntimeDelegatable(overridesRegistry, documentKeyStoreOperationsImplRuntimeDelegatable());
        }
        private ProfileStoreService profileStoreService() {
            return new ProfileStoreService(gsonSerde(),  userProfileCache(), config, bucketAccessServiceImplRuntimeDelegatable(), storage);
        }
        public DocumentEncryption documentEncryption(Properties properties) {
            return new DocumentEncryption(properties, new CMSDocumentWriteServiceRuntimeDelegatable(overridesRegistry, storage, cmsEncryptionServiceImplRuntimeDelegatable()),
                    new CMSDocumentReadServiceRuntimeDelegatable(overridesRegistry, storage, privateKeyServiceImplRuntimeDelegatable() , cmsEncryptionServiceImplRuntimeDelegatable()));
        }

        public KeyStoreOper keyStoreOper(Properties properties){
            return new KeyStoreOper(properties, config, storage, keyCreationConfig());
        }

        public Userprofile userprofile() {
            return new Userprofile(config, profileStoreService());
        }

    }
}
