package de.adorsys.datasafe.cli.config;

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
import de.adorsys.datasafe.types.api.utils.ExecutorServiceUtil;
import lombok.experimental.Delegate;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class DatasafeFactory {

    public static DefaultDatasafeServices datasafe(Path fsRoot, String systemPassword) {
        OverridesRegistry registry = new BaseOverridesRegistry();
        DefaultDatasafeServices multiDfsDatasafe = DaggerDefaultDatasafeServices
                .builder()
                .config(new DefaultDFSConfig(fsRoot.toUri(), systemPassword))
                .storage(
                        new RegexDelegatingStorage(
                                ImmutableMap.<Pattern, StorageService>builder()
                                        .put(Pattern.compile("file:/.+"), localFs(fsRoot))
                                        .put(Pattern.compile("s3://.+"), amazonS3()).build()
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

    private static StorageService amazonS3() {
        return new UriBasedAuthStorageService(
                acc -> new S3StorageService(
                        S3ClientFactory.getClientByRegion(
                                acc.getOnlyHostPart().toString().split("://")[1],
                                acc.getAccessKey(),
                                acc.getSecretKey()
                        ),
                        // Bucket name is encoded in first path segment
                        acc.getBucketName(),
                        ExecutorServiceUtil.submitterExecutesOnStarvationExecutingService()
                )
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
}
