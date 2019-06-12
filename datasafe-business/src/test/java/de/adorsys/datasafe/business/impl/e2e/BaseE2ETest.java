package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRemovalService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.inbox.api.actions.ListInbox;
import de.adorsys.datasafe.inbox.api.actions.ReadFromInbox;
import de.adorsys.datasafe.inbox.api.actions.RemoveFromInbox;
import de.adorsys.datasafe.inbox.api.actions.WriteToInbox;
import de.adorsys.datasafe.privatestore.api.actions.ListPrivate;
import de.adorsys.datasafe.privatestore.api.actions.ReadFromPrivate;
import de.adorsys.datasafe.privatestore.api.actions.RemoveFromPrivate;
import de.adorsys.datasafe.privatestore.api.actions.WriteToPrivate;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.utils.Log;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseE2ETest extends BaseMockitoTest {

    protected static final String PRIVATE_COMPONENT = "private";
    protected static final String PRIVATE_FILES_COMPONENT = PRIVATE_COMPONENT + "/files";
    protected static final String PUBLIC_COMPONENT = "public";
    protected static final String INBOX_COMPONENT = PUBLIC_COMPONENT + "/" + "inbox";
    protected static final String VERSION_COMPONENT = "versions";

    protected DFSConfig dfsConfig;
    protected ListPrivate listPrivate;
    protected ReadFromPrivate readFromPrivate;
    protected WriteToPrivate writeToPrivate;
    protected RemoveFromPrivate removeFromPrivate;
    protected ReadFromInbox readFromInbox;
    protected ListInbox listInbox;
    protected WriteToInbox writeToInbox;
    protected RemoveFromInbox removeFromInbox;
    protected ProfileRegistrationService profileRegistrationService;
    protected ProfileRemovalService profileRemovalService;
    protected ProfileRetrievalService profileRetrievalService;

    protected UserIDAuth john;
    protected UserIDAuth jane;

    protected void initialize(DFSConfig dfsConfig, DefaultDatasafeServices datasafeServices) {
        this.dfsConfig = dfsConfig;
        this.listPrivate = datasafeServices.privateService();
        this.readFromPrivate = datasafeServices.privateService();
        this.writeToPrivate = datasafeServices.privateService();
        this.removeFromPrivate = datasafeServices.privateService();
        this.readFromInbox = datasafeServices.inboxService();
        this.listInbox = datasafeServices.inboxService();
        this.writeToInbox = datasafeServices.inboxService();
        this.removeFromInbox = datasafeServices.inboxService();
        this.profileRegistrationService = datasafeServices.userProfile();
        this.profileRemovalService = datasafeServices.userProfile();
        this.profileRetrievalService = datasafeServices.userProfile();
    }

    protected void initialize(DFSConfig dfsConfig, VersionedDatasafeServices datasafeServices) {
        this.dfsConfig = dfsConfig;
        this.listPrivate = datasafeServices.latestPrivate();
        this.readFromPrivate = datasafeServices.latestPrivate();
        this.writeToPrivate = datasafeServices.latestPrivate();
        this.removeFromPrivate = datasafeServices.latestPrivate();
        this.readFromInbox = datasafeServices.inboxService();
        this.listInbox = datasafeServices.inboxService();
        this.writeToInbox = datasafeServices.inboxService();
        this.removeFromInbox = datasafeServices.inboxService();
        this.profileRegistrationService = datasafeServices.userProfile();
        this.profileRemovalService = datasafeServices.userProfile();
        this.profileRetrievalService = datasafeServices.userProfile();
    }

    @SneakyThrows
    protected void writeDataToPrivate(UserIDAuth auth, String path, String data) {
        OutputStream stream = writeToPrivate.write(WriteRequest.forDefaultPrivate(auth, path));
        stream.write(data.getBytes());
        stream.close();
        log.info("File {} of user {} saved to {}", Log.secure(data), Log.secure(auth),
                Log.secure(path.split("/"), "/"));
    }

    @SneakyThrows
    protected void writeDataToInbox(UserIDAuth auth, String path, String data) {
        OutputStream stream = writeToInbox.write(WriteRequest.forDefaultPublic(auth.getUserID(), path));
        stream.write(data.getBytes());
        stream.close();
        log.info("File {} of user {} saved to {}", Log.secure(data), Log.secure(auth),
                Log.secure(path.split("/"), "/"));
    }

    protected AbsoluteLocation<ResolvedResource> getFirstFileInPrivate(UserIDAuth owner) {
        return getAllFilesInPrivate(owner).get(0);
    }

    protected List<AbsoluteLocation<ResolvedResource>> getAllFilesInPrivate(UserIDAuth owner) {
        List<AbsoluteLocation<ResolvedResource>> files = listPrivate.list(
                ListRequest.forDefaultPrivate(owner, "./")
        ).collect(Collectors.toList());

        log.info("{} has {} in PRIVATE", owner.getUserID(), Log.secure(files));
        return files;
    }

    @SneakyThrows
    protected String readPrivateUsingPrivateKey(UserIDAuth user, PrivateResource location) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream dataStream = readFromPrivate.read(ReadRequest.forPrivate(user, location));

        ByteStreams.copy(dataStream, outputStream);
        String data = new String(outputStream.toByteArray());
        log.info("{} has {} in PRIVATE", user, Log.secure(data));

        return data;
    }

    protected void removeFromPrivate(UserIDAuth user, PrivateResource location) {
        removeFromPrivate.remove(RemoveRequest.forPrivate(user, location));
    }

    @SneakyThrows
    protected String readInboxUsingPrivateKey(UserIDAuth user, PrivateResource location) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream dataStream = readFromInbox.read(ReadRequest.forPrivate(user, location));

        ByteStreams.copy(dataStream, outputStream);
        String data = new String(outputStream.toByteArray());
        log.info("{} has {} in INBOX", user, Log.secure(data));

        return data;
    }

    protected AbsoluteLocation<ResolvedResource> getFirstFileInInbox(UserIDAuth inboxOwner) {
        return getAllFilesInInbox(inboxOwner).get(0);
    }

    protected List<AbsoluteLocation<ResolvedResource>> getAllFilesInInbox(UserIDAuth inboxOwner) {
        List<AbsoluteLocation<ResolvedResource>> files = listInbox.list(
                ListRequest.forDefaultPrivate(inboxOwner, "./")
        ).collect(Collectors.toList());

        log.info("{} has {} in INBOX", inboxOwner, Log.secure(files));
        return files;
    }

    protected void registerJohnAndJane() {
        john = registerUser("john");
        jane = registerUser("jane");
    }

    @SneakyThrows
    protected void sendToInbox(UserID to, String filename, String data) {
        OutputStream stream = writeToInbox.write(WriteRequest.forDefaultPublic(to, "./" + filename));
        stream.write(data.getBytes());
        stream.close();
        log.info("File {} sent to INBOX of user {}", Log.secure(filename), to);
    }

    protected void removeFromInbox(UserIDAuth inboxOwner, PrivateResource location) {
        removeFromInbox.remove(RemoveRequest.forPrivate(inboxOwner, location));
    }

    protected UserIDAuth registerUser(String userName) {
        UserIDAuth auth = new UserIDAuth(new UserID(userName), new ReadKeyPassword("secure-password " + userName));

        profileRegistrationService.registerPublic(dfsConfig.defaultPublicTemplate(auth));
        profileRegistrationService.registerPrivate(dfsConfig.defaultPrivateTemplate(auth));

        log.info("Created user: {}", Log.secure(userName));
        return auth;
    }

    protected UserIDAuth createJohnTestUser(int i) {
        UserID userName = new UserID("john_" + i);

        return new UserIDAuth(
                userName,
                new ReadKeyPassword("secure-password " + userName.getValue())
        );
    }

    protected void assertPrivateSpaceList(UserIDAuth user, String root, String... expected) {
        List<String> paths = listPrivate.list(ListRequest.forDefaultPrivate(user, root))
                .map(it -> it.getResource().asPrivate().decryptedPath().toASCIIString())
                .collect(Collectors.toList());

        for (String toFind : expected) {
            assertThat(paths.stream().anyMatch(it -> it.equals(toFind))).isTrue();
        }
    }

    protected void assertInboxSpaceList(UserIDAuth user, String root, String... expected) {
        List<String> paths = listInbox.list(ListRequest.forDefaultPrivate(user, root))
                .map(it -> it.getResource().asPrivate().decryptedPath().toASCIIString())
                .collect(Collectors.toList());

        for (String toFind : expected) {
            assertThat(paths.stream().anyMatch(it -> it.equals(toFind))).isTrue();
        }
    }

    @SneakyThrows
    protected void assertRootDirIsEmpty(WithStorageProvider.StorageDescriptor descriptor) {
        assertThat(descriptor.getStorageService().get()
                .list(
                        new AbsoluteLocation<>(BasePrivateResource.forPrivate(descriptor.getLocation()))
                )
        ).isEmpty();
        if (descriptor.getStorageService().get() instanceof FileSystemStorageService) {
            // additional check that directory contains only folders that were created recursively
            // we do not have permission to delete these - because we removed files linked to profile
            // however we can't remove anything above
            assertThat(Files.walk(Paths.get(descriptor.getLocation().asURI())))
                    .allMatch(it -> it.toFile().isDirectory())
                    .extracting(Path::toUri)
                    .extracting(it -> descriptor.getLocation().asURI().relativize(it))
                    .extracting(URI::toString)
                    .containsExactlyInAnyOrder(
                            "",
                            "john/",
                            "john/public/",
                            "john/private/",
                            "profiles/",
                            "profiles/public/",
                            "profiles/private/"
                    );
        }
    }
}
