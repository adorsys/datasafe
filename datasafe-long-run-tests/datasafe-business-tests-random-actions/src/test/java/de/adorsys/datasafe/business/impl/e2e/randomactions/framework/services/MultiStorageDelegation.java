package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.collect.ImmutableMap;
import dagger.Lazy;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Fixture;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Operation;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.OperationType;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.impl.profile.config.DFSConfigWithStorageCreds;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.dfs.RegexAccessServiceWithStorageCredentialsImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.storage.api.RegexDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.api.UriBasedAuthStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3ClientFactory;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class MultiStorageDelegation {

    private final List<WithStorageProvider.StorageDescriptor> descriptors;
    private final Fixture fixture;
    private static AmazonS3 directoryClient = null;

    public DefaultDatasafeServices getDatasafeServices() {
        DefaultDatasafeServices multiDfsDatasafe = null;
        ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);


        // Create all required S3 buckets:
        /*descriptors.stream().forEach(it -> {
            String endpoint = it.getLocation().getWrapped().toString();
            log.info("MINIO for {} is available at: {} with access: '{}'/'{}'", it, endpoint, it.getAccessKey(),
                    it.getSecretKey());

            AmazonS3 client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(it.getAccessKey(), it.getSecretKey()))
                    )
                    .withRegion(it.getRegion())
                    .build();

            client.getBucketAcl(it.getRootBucket().split("/")[0].toString());
            //client.createBucket(it.getRootBucket().split("/")[0].toString());

            //if (it.equals(AMAZON_MULTI_BUCKET)) {
            directoryClient = client;
            //}
        });

        Security.addProvider(new BouncyCastleProvider());
        String directoryBucketS3Uri = "s3://" + descriptors.get(0).getRootBucket().split("/")[0].toString() + "/";
        // static client that will be used to access `directory` bucket:
        StorageService directoryStorage = new S3StorageService(
                directoryClient,
                descriptors.get(0).getRootBucket(),
                EXECUTOR
        );*/

        /*DefaultDatasafeServices multiDfsDatasafe = DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(descriptors.get(0).getLocation(), "PAZZWORT"))
                .storage(descriptors.get(0).getStorageService().get())
                .build();*/

        OverridesRegistry registry = new BaseOverridesRegistry();

        List<Operation> users = fixture.getUserPrivateSpace().keySet().stream()
                .map(it -> Operation.builder().type(OperationType.CREATE_USER).userId(it).build())
                .collect(Collectors.toList());
        for(Operation opr: users){
            WithStorageProvider.StorageDescriptor descriptor = descriptors.get(Integer.parseInt(opr.getUserId().split("-")[1]) % descriptors.size());

            Security.addProvider(new BouncyCastleProvider());
            String directoryBucketS3Uri = "s3://" + descriptor.getRootBucket() + "/";

            AmazonS3 client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(descriptor.getAccessKey(), descriptor.getSecretKey()))
                    )
                    .withRegion(descriptor.getRegion())
                    .build();

            // static client that will be used to access `directory` bucket:
            StorageService directoryStorage = new S3StorageService(
                    client,
                    descriptor.getRootBucket(),
                    EXECUTOR
            );

            multiDfsDatasafe = DaggerDefaultDatasafeServices
                    .builder()
                    .config(new DFSConfigWithStorageCreds(directoryBucketS3Uri, "PAZZWORT"))
                    // This storage service will route requests to proper bucket based on URI content:
                    // URI with directoryBucket to `directoryStorage`
                    // URI with filesBucketOne will get dynamically generated S3Storage
                    // URI with filesBucketTwo will get dynamically generated S3Storage
                    .storage(
                            new RegexDelegatingStorage(
                                    ImmutableMap.<Pattern, StorageService>builder()
                                            // bind URI that contains `directoryBucket` to directoryStorage
                                            .put(Pattern.compile(directoryBucketS3Uri + ".+"), directoryStorage)
                                            .put(
                                                    Pattern.compile(opr.getUserId()),
                                                    // Dynamically creates S3 client with bucket name equal to username
                                                    new UriBasedAuthStorageService(
                                                            acc -> new S3StorageService(
                                                                    AmazonS3ClientBuilder.standard()
                                                                            .withCredentials(new AWSStaticCredentialsProvider(
                                                                                    new BasicAWSCredentials(descriptor.getAccessKey(), descriptor.getSecretKey()))
                                                                            )
                                                                            //.withRegion(acc.getRegion())
                                                                            .build(),
                                                                    // Bucket name is encoded in first path segment
                                                                    descriptor.getRootBucket(),
                                                                    EXECUTOR
                                                            )
                                                    )
                                            ).build()
                            )
                    )
                    .overridesRegistry(registry)
                    .build();
            // Instead of default BucketAccessService we will use service that reads storage access credentials from
            // keystore
            BucketAccessServiceImplRuntimeDelegatable.overrideWith(
                    registry, args -> new WithCredentialProvider(args.getStorageKeyStoreOperations())
            );


            // John will have all his private files stored on `filesBucketOne` and `filesBucketOne`.
            // Depending on path of file - filesBucketOne or filesBucketTwo - requests will be routed to proper bucket.
            // I.e. path filesBucketOne/path/to/file will end up in `filesBucketOne` with key path/to/file
            // his profile and access credentials for `filesBucketOne`  will be in `configBucket`
            UserIDAuth user = new UserIDAuth(opr.getUserId(), "");

            // Here, nothing expects John has own storage credentials:
            multiDfsDatasafe.userProfile().registerUsingDefaults(user);

            // Tell system that John will use his own storage credentials - regex match:
            StorageIdentifier bucketOne = new StorageIdentifier(descriptor+ ".+");
            // Set location for John's credentials keystore and put storage credentials into it:
            UserPrivateProfile profile = multiDfsDatasafe.userProfile().privateProfile(user);
            profile.getPrivateStorage().put(
                    bucketOne,
                    new AbsoluteLocation<>(BasePrivateResource.forPrivate(descriptor.getLocation().getWrapped().toString()+ "/"))
            );
            multiDfsDatasafe.userProfile().updatePrivateProfile(user, profile);

            // register John's DFS access for `filesBucketOne` minio bucket
            multiDfsDatasafe.userProfile().registerStorageCredentials(
                    user,
                    bucketOne,
                    new StorageCredentials(
                            descriptor.getAccessKey(),
                            descriptor.getSecretKey()
                    )
            );
        }

        return multiDfsDatasafe;
    }

    private static class WithCredentialProvider extends BucketAccessServiceImpl {

        @Delegate
        private final RegexAccessServiceWithStorageCredentialsImpl delegate;

        private WithCredentialProvider(Lazy<StorageKeyStoreOperations> storageKeyStoreOperations) {
            super(null);
            this.delegate = new RegexAccessServiceWithStorageCredentialsImpl(storageKeyStoreOperations);
        }
    }

    @Getter
    enum BucketId {
        AMAZON_MULTI_BUCKET;

        private final String accessKey;
        private final String secretKey;
        private final String bucketName;

        BucketId() {
            this.accessKey = "access-" + toString();
            this.secretKey = "secret-" + toString();
            this.bucketName = toString();
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase().replaceAll("_", "");
        }
    }
}
