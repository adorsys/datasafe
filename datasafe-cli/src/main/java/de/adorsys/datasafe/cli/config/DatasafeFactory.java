package de.adorsys.datasafe.cli.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.collect.ImmutableMap;
import dagger.Lazy;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.dfs.RegexAccessServiceWithStorageCredentialsImpl;
import de.adorsys.datasafe.storage.api.RegexDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.api.UriBasedAuthStorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3ClientFactory;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ExecutorServiceUtil;
import lombok.experimental.Delegate;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.regex.Pattern;

@Slf4j
@UtilityClass
public class DatasafeFactory {

    public static DefaultDatasafeServices datasafe(Path fsRoot, ReadStorePassword systemPassword) {
        OverridesRegistry registry = new BaseOverridesRegistry();
        DefaultDatasafeServices multiDfsDatasafe = DaggerDefaultDatasafeServices
                .builder()
                .config(new DefaultDFSConfig(fsRoot.toUri(), systemPassword))
                .storage(
                        new RegexDelegatingStorage(
                                ImmutableMap.<Pattern, StorageService>builder()
                                        .put(Pattern.compile("file:/.+"), localFs(fsRoot))
                                        .put(Pattern.compile("s3://.+"), amazonS3())
                                        .put(Pattern.compile("http://.+"), httpS3())
                                        .put(Pattern.compile("https://.+"), httpS3())
                                        .build()
                        )
                )
                .overridesRegistry(registry)
                .build();

        BucketAccessServiceImplRuntimeDelegatable.overrideWith(
                registry, args -> new WithCredentialProvider(args.getStorageKeyStoreOperations())
        );

        return multiDfsDatasafe;
    }

    private static StorageService localFs(Path fsRoot) {
        return new FileSystemStorageService(fsRoot);
    }

    private static StorageService httpS3() {
        return new UriBasedAuthStorageService(
                acc -> getStorageService(
                        acc.getAccessKey(),
                        acc.getSecretKey(),
                        acc.getEndpoint(),
                        acc.getRegion(),
                        acc.getBucketName()
                )
        );
    }

    private static StorageService amazonS3() {
        return new UriBasedAuthStorageService(
                acc -> new S3StorageService(
                        S3ClientFactory.getAmazonClient(
                                acc.getRegion(),
                                acc.getAccessKey(),
                                acc.getSecretKey()
                        ),
                        // Bucket name is encoded in first path segment
                        acc.getBucketName(),
                        ExecutorServiceUtil.submitterExecutesOnStarvationExecutingService()
                ),
                uri -> (uri.getHost() + "/" + uri.getPath().replaceFirst("^/", "")).split("/")
        );
    }

    private static class WithCredentialProvider extends BucketAccessServiceImpl {

        @Delegate
        private final RegexAccessServiceWithStorageCredentialsImpl delegate;

        private WithCredentialProvider(Lazy<StorageKeyStoreOperations> storageKeyStoreOperations) {
            super(null);
            this.delegate = new RegexAccessServiceWithStorageCredentialsImpl(storageKeyStoreOperations);
        }
    }

    private static S3StorageService getStorageService(String accessKey, String secretKey, String url, String region,
                                                      String bucket) {
        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                        accessKey,
                                        secretKey))
                )
                .enablePathStyleAccess();

        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(
                url,
                region
        );
        amazonS3ClientBuilder.withEndpointConfiguration(endpoint);

        if (! url.toLowerCase().startsWith("https")) {
            log.info("Creating S3 client without https");
            ClientConfiguration clientConfig = new ClientConfiguration();
            clientConfig.setProtocol(Protocol.HTTP);
            clientConfig.disableSocketProxy();
            amazonS3ClientBuilder.withClientConfiguration(clientConfig);
        }

        AmazonS3 amazons3 = amazonS3ClientBuilder.build();

        return new S3StorageService(
                amazons3,
                bucket,
                ExecutorServiceUtil
                        .submitterExecutesOnStarvationExecutingService(
                                5,
                                5
                        )
        );
    }
}

