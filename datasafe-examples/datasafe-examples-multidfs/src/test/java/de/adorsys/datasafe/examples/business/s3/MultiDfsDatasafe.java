package de.adorsys.datasafe.examples.business.s3;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileOperations;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.inbox.api.InboxService;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.storage.api.UriBasedAuthStorageService;
import de.adorsys.datasafe.storage.impl.s3.HostBasedBucketRouter;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.util.io.Streams;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiDfsDatasafe implements DefaultDatasafeServices {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    private final DatasafeBasedCredentialsManager credentialsManager;
    private final DefaultDatasafeServices storageCredentialsDatasafe;
    private final DefaultDatasafeServices fileStorageDatasafe;
    private final ProfileOperationsChain profileOperationsChain;

    public MultiDfsDatasafe(
            AmazonS3 credentialsStorage,
            String credentialsBucketName) {
        this.storageCredentialsDatasafe = DaggerDefaultDatasafeServices
                .builder()
                .config(new DefaultDFSConfig(credentialsBucketName, "PAZZWORT"))
                .storage(new S3StorageService(
                        credentialsStorage,
                        credentialsBucketName,
                        EXECUTOR)
                ).build();

        OverridesRegistry overridesRegistry = new BaseOverridesRegistry();
        this.fileStorageDatasafe = DaggerDefaultDatasafeServices
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

        DatasafeBasedCredentialsManager manager = new DatasafeBasedCredentialsManager(this.storageCredentialsDatasafe);
        BucketAccessServiceImplRuntimeDelegatable.overrideWith(overridesRegistry, args -> manager);



        this.credentialsManager = new DatasafeBasedCredentialsManager(storageCredentialsDatasafe);
        this.profileOperationsChain = new ProfileOperationsChain();
    }

    @SneakyThrows
    void registerDfs(UserIDAuth forUser, String bucketName, StorageCredentials credentials) {
        credentialsManager.registerDfs(forUser, bucketName, credentials);
    }

    @Override
    public PrivateSpaceService privateService() {
        return fileStorageDatasafe.privateService();
    }

    @Override
    public InboxService inboxService() {
        return fileStorageDatasafe.inboxService();
    }

    @Override
    public ProfileOperations userProfile() {
        return profileOperationsChain;
    }

    private class ProfileOperationsChain implements ProfileOperations {

        @Override
        public void registerPublic(CreateUserPublicProfile profile) {
            // NOP
        }

        @Override
        public void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword) {
            storageCredentialsDatasafe.userProfile().updateReadKeyPassword(forUser, newPassword);
            fileStorageDatasafe.userProfile().updateReadKeyPassword(forUser, newPassword);
        }

        @Override
        public void registerPrivate(CreateUserPrivateProfile profile) {
            // NOP
        }

        @Override
        public void registerUsingDefaults(UserIDAuth user) {
            storageCredentialsDatasafe.userProfile().registerUsingDefaults(user);
            fileStorageDatasafe.userProfile().registerUsingDefaults(user);
        }

        @Override
        public void deregister(UserIDAuth userID) {
            fileStorageDatasafe.userProfile().deregister(userID);
            storageCredentialsDatasafe.userProfile().deregister(userID);
        }

        @Override
        public UserPublicProfile publicProfile(UserID ofUser) {
            return storageCredentialsDatasafe.userProfile().publicProfile(ofUser);
        }

        @Override
        public UserPrivateProfile privateProfile(UserIDAuth ofUser) {
            return storageCredentialsDatasafe.userProfile().privateProfile(ofUser);
        }

        @Override
        public boolean userExists(UserID ofUser) {
            return storageCredentialsDatasafe.userProfile().userExists(ofUser);
        }
    }

    @RequiredArgsConstructor
    private static class DatasafeBasedCredentialsManager extends BucketAccessServiceImpl {

        private final DefaultDatasafeServices credentialStorageDatasafe;

        @SneakyThrows
        void registerDfs(UserIDAuth forUser, String bucketName, StorageCredentials credentials) {
            if (credentialStorageDatasafe.userProfile().userExists(forUser.getUserID())) {
                credentialStorageDatasafe.userProfile().registerUsingDefaults(forUser);
            }

            try (OutputStream os = credentialStorageDatasafe
                    .privateService()
                    .write(WriteRequest.forDefaultPrivate(forUser, bucketName))) {
                os.write(credentials.serialize().getBytes(StandardCharsets.UTF_8));
            }
        }

        @Override
        public AbsoluteLocation<PrivateResource> privateAccessFor(UserIDAuth user, PrivateResource resource) {
            return super.privateAccessFor(user, resource);
        }

        @Override
        public AbsoluteLocation<PublicResource> publicAccessFor(UserID user, PublicResource resource) {
            return super.publicAccessFor(user, resource);
        }

        @Override
        public AbsoluteLocation withSystemAccess(AbsoluteLocation resource) {
            return super.withSystemAccess(resource);
        }

        @SneakyThrows
        StorageCredentials readCredentials(UserIDAuth forUser, String bucketName) {
            try (InputStream is = credentialStorageDatasafe
                    .privateService()
                    .read(ReadRequest.forDefaultPrivate(forUser, bucketName))) {
                return new StorageCredentials(new String(Streams.readAll(is)));
            }
        }
    }

    @Data
    @AllArgsConstructor
    static class StorageCredentials {

        private final String endpointUri;
        private final String username;
        private final String password;

        StorageCredentials(String credentialStr) {
            String[] hostUsernamePassword = credentialStr.split("\0");
            this.endpointUri = hostUsernamePassword[0];
            this.username = hostUsernamePassword[1];
            this.password = hostUsernamePassword[2];
        }

        String serialize() {
            return endpointUri + "\0" + username + "\0" + password;
        }
    }
}
