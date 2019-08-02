package de.adorsys.datasafe.business.impl.e2e.randomactions;

import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.BaseRandomActions;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileOperations;
import de.adorsys.datasafe.directory.api.types.*;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.inbox.api.InboxService;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.*;
import de.adorsys.datasafe.simple.adapter.impl.DFSTestCredentialsFactory;
import de.adorsys.datasafe.simple.adapter.impl.SimpleDatasafeServiceImpl;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;

import static de.adorsys.datasafe.business.impl.e2e.randomactions.framework.BaseRandomActions.DISABLE_RANDOM_ACTIONS_TEST;

/**
 * Executes random user actions in multiple threads against Datasafe-core.
 * We have action fixture for 10 users, where each user does share,read,write,etc. After one executes
 * actions in this fixture he can validate inbox and private directory content using fixture expectation section.
 * This fixture is duplicated N times and submitted to thread pool, so any thread in pool can pick some action and
 * act independently of others. Thread actions and expectations are prefixed with execution id.
 * Imitates close-to-production SimpleDatasafeAdapter deployment.
 */
@DisabledIfSystemProperty(named = DISABLE_RANDOM_ACTIONS_TEST, matches = "true")
class RandomActionsOnSimpleDatasafeAdapterTest extends BaseRandomActions {

    @ParameterizedTest
    @MethodSource("actionsOnSoragesAndThreadsAndFilesizes")
    void testRandomActionsParallelThreads(StorageDescriptor descriptor, int threadCount, int filesizeInMb) {
        DefaultDatasafeServices datasafeServices = datasafeServicesFromSimpleDatasafeAdapter(descriptor);
        StatisticService statisticService = new StatisticService();

        executeTest(
                smallSimpleDocusafeAdapterFixture(),
                descriptor.getName(),
                filesizeInMb,
                threadCount,
                datasafeServices.userProfile(),
                datasafeServices.privateService(),
                datasafeServices.inboxService(),
                statisticService
        );
    }

    private DefaultDatasafeServices datasafeServicesFromSimpleDatasafeAdapter(StorageDescriptor descriptor) {
        SimpleDatasafeService datasafeService = new SimpleDatasafeServiceImpl(
            DFSTestCredentialsFactory.credentials(descriptor)
        );

        return new DefaultDatasafeServices() {
            @Override
            public PrivateSpaceService privateService() {
                return new PrivateSpaceService() {
                    @Override
                    public Stream<AbsoluteLocation<ResolvedResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
                        return datasafeService.list(
                                request.getOwner(),
                                asFqnDir(request.getLocation()),
                                ListRecursiveFlag.TRUE
                        ).stream().map(it -> new AbsoluteLocation<>(asResolved(descriptor.getLocation(), it)));
                    }

                    @Override
                    public InputStream read(ReadRequest<UserIDAuth, PrivateResource> request) {
                        return new ByteArrayInputStream(
                                datasafeService.readDocument(
                                        request.getOwner(),
                                        asFqnDoc(request.getLocation())).getDocumentContent().getValue()
                        );
                    }

                    @Override
                    public void remove(RemoveRequest<UserIDAuth, PrivateResource> request) {
                        datasafeService.deleteFolder(request.getOwner(), asFqnDir(request.getLocation()));
                    }

                    @Override
                    public OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request) {
                        return new PutBlobOnClose(asFqnDoc(request.getLocation()), request.getOwner(), datasafeService);
                    }

                    @RequiredArgsConstructor
                    final class PutBlobOnClose extends ByteArrayOutputStream {

                        private final DocumentFQN documentFQN;
                        private final UserIDAuth userIDAuth;
                        private final SimpleDatasafeService datasafeService;

                        @Override
                        public void close() throws IOException {
                            super.close();
                            datasafeService.storeDocument(
                                    userIDAuth,
                                    new DSDocument(documentFQN, new DocumentContent(super.toByteArray()))
                            );
                        }
                    }
                };
            }

            @Override
            public InboxService inboxService() {
                return new InboxService() {
                    @Override
                    public Stream<AbsoluteLocation<ResolvedResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
                        return Stream.empty();
                    }

                    @Override
                    public InputStream read(ReadRequest<UserIDAuth, PrivateResource> request) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public void remove(RemoveRequest<UserIDAuth, PrivateResource> request) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public OutputStream write(WriteRequest<Set<UserID>, PublicResource> request) {
                        throw new IllegalStateException("Not implemented");
                    }
                };
            }

            @Override
            public ProfileOperations userProfile() {
                return new ProfileOperations() {

                    @Override
                    public void createDocumentKeystore(UserIDAuth user, UserPrivateProfile profile) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public void registerPublic(CreateUserPublicProfile profile) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public void registerPrivate(CreateUserPrivateProfile profile) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public void registerUsingDefaults(UserIDAuth user) {
                        datasafeService.createUser(user);
                    }

                    @Override
                    public void registerStorageCredentials(
                            UserIDAuth user, StorageIdentifier storageId, StorageCredentials credentials) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public void deregister(UserIDAuth userID) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public void createAllAllowableKeystores(UserIDAuth user, UserPrivateProfile profile) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public void createStorageKeystore(UserIDAuth user) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public UserPublicProfile publicProfile(UserID ofUser) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public UserPrivateProfile privateProfile(UserIDAuth ofUser) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public void updatePublicProfile(UserIDAuth forUser, UserPublicProfile profile) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public void updatePrivateProfile(UserIDAuth forUser, UserPrivateProfile profile) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public void deregisterStorageCredentials(UserIDAuth user, StorageIdentifier storageId) {
                        throw new IllegalStateException("Not implemented");
                    }

                    @Override
                    public boolean userExists(UserID ofUser) {
                        throw new IllegalStateException("Not implemented");
                    }
                };
            }
        };
    }

    private DocumentDirectoryFQN asFqnDir(PrivateResource resource) {
        return new DocumentDirectoryFQN(resource.location().getPath());
    }

    private DocumentFQN asFqnDoc(PrivateResource resource) {
        return new DocumentFQN(resource.location().getPath());
    }

    private ResolvedResource asResolved(Uri root, DocumentFQN resource) {
        return new BaseResolvedResource(
                new BasePrivateResource(root, new Uri(""), new Uri(resource.getDatasafePath())),
                Instant.now()
        );
    }

    private DFSCredentials getCredentials(StorageDescriptor descriptor) {
        switch (descriptor.getName()) {
            case FILESYSTEM: {
                return FilesystemDFSCredentials.builder()
                        .root(descriptor.getRootBucket())
                        .build();
            }
            case MINIO:
            case CEPH:
            case AMAZON: {
                descriptor.getStorageService().get();
                return AmazonS3DFSCredentials.builder()
                        .accessKey(descriptor.getAccessKey())
                        .secretKey(descriptor.getSecretKey())
                        .region(descriptor.getRegion())
                        .rootBucket(descriptor.getRootBucket())
                        .url(descriptor.getMappedUrl())
                        .build();
            }
            default:
                throw new SimpleAdapterException("missing switch for " + descriptor.getName());
        }
    }
}
