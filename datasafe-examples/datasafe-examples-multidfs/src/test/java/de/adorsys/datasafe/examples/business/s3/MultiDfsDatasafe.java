package de.adorsys.datasafe.examples.business.s3;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileOperations;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRetrievalServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.inbox.api.InboxService;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.storage.api.UriBasedAuthStorageService;
import de.adorsys.datasafe.storage.impl.s3.HostBasedBucketRouter;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import lombok.SneakyThrows;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class delegates work to 2 instances of Datasafe. First one is Directory Datasafe that stores user profile -
 * where are users' private files and users' access credentials to different storage in encrypted form. Second one
 * is actually storing users' private files and INBOX data.
 */
public class MultiDfsDatasafe implements DefaultDatasafeServices {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    private final DatasafeBasedCredentialsManager credentialsManager;
    private final DefaultDatasafeServices directoryDatasafe;
    private final DefaultDatasafeServices datasafe;

    public MultiDfsDatasafe(
            AmazonS3 credentialsStorage,
            String credentialsBucketName) {
        this.directoryDatasafe = createDirectoryDatasafe(credentialsStorage, credentialsBucketName);
        OverridesRegistry overridesRegistry = new BaseOverridesRegistry();
        this.datasafe = createUsersDatasafe(overridesRegistry);

        DatasafeBasedCredentialsManager manager = new DatasafeBasedCredentialsManager(this.directoryDatasafe);
        // storage access credentials are read from Directory Datasafe service:
        BucketAccessServiceImplRuntimeDelegatable.overrideWith(overridesRegistry, args -> manager);
        // user profile is read from Directory Datasafe service:
        ProfileRetrievalServiceImplRuntimeDelegatable.overrideWith(
                overridesRegistry,
                args -> new DatasafeBasedProfileManager(directoryDatasafe)
        );

        this.credentialsManager = new DatasafeBasedCredentialsManager(directoryDatasafe);
    }

    // Creates Datasafe that stores users' private files, this instance will query Directory datasafe
    // to get users' profile and access credentials
    private DefaultDatasafeServices createUsersDatasafe(OverridesRegistry overridesRegistry) {
        return DaggerDefaultDatasafeServices
                .builder()
                .config(new DefaultDFSConfig("s3://bucket/", "PAZZWORT"))
                .overridesRegistry(overridesRegistry)
                .storage(new UriBasedAuthStorageService(
                        id -> new S3StorageService(
                                S3ClientFactory.getClient(
                                        id.getWithoutCreds().toString(), id.getAccessKey(), id.getSecretKey()
                                ),
                                new HostBasedBucketRouter(),
                                EXECUTOR
                        ))
                ).build();
    }

    // Creates Datasafe that stores user profiles and storage access credentials.
    private static DefaultDatasafeServices createDirectoryDatasafe(AmazonS3 credentialsStorage,
                                                                   String credentialsBucketName) {
        return DaggerDefaultDatasafeServices
                .builder()
                .config(new DefaultDFSConfig("s3://" + credentialsBucketName + "/", "PAZZWORT"))
                .storage(new S3StorageService(
                        credentialsStorage,
                        credentialsBucketName,
                        EXECUTOR)
                ).build();
    }

    @SneakyThrows
    void registerDfs(UserIDAuth forUser, String bucketName, StorageCredentials credentials) {
        credentialsManager.registerDfs(forUser, bucketName, credentials);
    }

    @Override
    public PrivateSpaceService privateService() {
        return datasafe.privateService();
    }

    @Override
    public InboxService inboxService() {
        return datasafe.inboxService();
    }

    @Override
    public ProfileOperations userProfile() {
        return directoryDatasafe.userProfile();
    }
}
