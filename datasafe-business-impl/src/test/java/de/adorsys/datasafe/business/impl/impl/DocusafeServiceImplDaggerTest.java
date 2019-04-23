package de.adorsys.datasafe.business.impl.impl;

import com.google.common.io.ByteStreams;
import com.google.common.io.MoreFiles;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPublicProfile;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import de.adorsys.datasafe.business.impl.BaseMockitoTest;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDocusafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDocusafeServices;
import de.adorsys.datasafe.business.impl.types.DefaultPrivateResource;
import de.adorsys.datasafe.business.impl.types.DefaultPublicResource;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DocusafeServiceImplDaggerTest extends BaseMockitoTest {

    private static final String MESSAGE_ONE = "Hello here";
    private static final String META_INFO = "This is metainfo here";

    private DefaultDocusafeServices docusafeService = DaggerDefaultDocusafeServices
            .builder()
            .storageList(this::listFiles)
            .storageRead(this::readFile)
            .storageWrite(this::writeFile)
            .build();

    private UserIDAuth john;
    private UserIDAuth jane;
    private Path tempDir;

    @Test
    void testWriteToPrivateListPrivateReadPrivateAndSendToAndReadFromInbox(@TempDir Path dfsLocation) {
        tempDir = dfsLocation;

        registerJohnAndJane();

        writeDataToPrivate(jane, "./secret.txt", MESSAGE_ONE, META_INFO);

        PrivateResource privateJane = getFirstFileInPrivate(jane);

        assertThat(((PrivateResourceWithMeta) privateJane).getMetaInformation()).isEqualTo(META_INFO);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane);

        sendToInbox(jane.getUserID(), john.getUserID(), "./hello.txt", privateContentJane);

        PrivateResource inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn);
        assertThat(result).isEqualTo(MESSAGE_ONE);
    }

    @SneakyThrows
    private void writeDataToPrivate(UserIDAuth auth, String path, String data, String meta) {
        OutputStream stream = docusafeService.privateService().write(
                WriteRequest.<UserIDAuth, PrivateResource>builder()
                        .owner(auth)
                        .location(new PrivateResourceWithMeta(new URI(path), meta))
                        .build()
        );

        stream.write(data.getBytes());
        stream.close();
    }

    @SneakyThrows
    private PrivateResource getFirstFileInPrivate(UserIDAuth inboxOwner) {
        List<PrivateResource> files = docusafeService.privateService().list(
                ListRequest.<UserIDAuth>builder()
                        .owner(inboxOwner)
                        .location(DefaultPrivateResource.ROOT)
                        .build()
        ).collect(Collectors.toList());
        log.info("{} has {} in PRIVATE", inboxOwner.getUserID().getValue(), files);
        return files.get(0);
    }

    @SneakyThrows
    private String readPrivateUsingPrivateKey(UserIDAuth user, PrivateResource location) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream dataStream = docusafeService.privateService()
            .read(ReadRequest.<UserIDAuth>builder()
                    .location(location)
                    .owner(user)
                    .build()
            );

        ByteStreams.copy(dataStream, outputStream);
        String data = new String(outputStream.toByteArray());
        log.info("{} has {} in PRIVATE", user.getUserID().getValue(), data);

        return data;
    }

    @SneakyThrows
    private String readInboxUsingPrivateKey(UserIDAuth user, PrivateResource location) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream dataStream = docusafeService.inboxService()
            .read(ReadRequest.<UserIDAuth>builder()
                    .owner(user)
                    .location(location)
                    .build()
            );

        ByteStreams.copy(dataStream, outputStream);
        String data = new String(outputStream.toByteArray());
        log.info("{} has {} in INBOX", user.getUserID().getValue(), data);

        return data;
    }

    @SneakyThrows
    private PrivateResource getFirstFileInInbox(UserIDAuth inboxOwner) {
        List<PrivateResource> files = docusafeService.inboxService().list(ListRequest.<UserIDAuth>builder()
                .owner(inboxOwner)
                .location(DefaultPrivateResource.ROOT)
                .build()
        ).collect(Collectors.toList());
        log.info("{} has {} in INBOX", inboxOwner.getUserID().getValue(), files);
        return files.get(0);
    }

    private void registerJohnAndJane() {
        john = registerUser("john");
        jane = registerUser("jane");
    }

    @SneakyThrows
    private void sendToInbox(UserID from, UserID to, String filename, String data) {
        OutputStream stream = docusafeService.inboxService().write(
            WriteRequest.<UserID, PublicResource>builder()
                    .location(new DefaultPublicResource(new URI("./" + filename)))
                    .owner(to)
                    .build()
        );

        stream.write(data.getBytes());
        stream.close();
    }

    @SneakyThrows
    private UserIDAuth registerUser(String userName) {
        UserIDAuth auth = new UserIDAuth();
        auth.setUserID(new UserID(userName));
        auth.setReadKeyPassword(new ReadKeyPassword("secure-password " + userName));

        docusafeService.userProfile().registerPublic(CreateUserPublicProfile.builder()
            .id(auth.getUserID())
            .inbox(access(new URI("./bucket/" + userName + "/").resolve("./inbox/")))
            .publicKeys(access(new URI("./bucket/" + userName + "/").resolve("./keystore")))
            .build()
        );

        docusafeService.userProfile().registerPrivate(CreateUserPrivateProfile.builder()
            .id(auth)
            .privateStorage(accessPrivate(new URI("./bucket/" + userName + "/").resolve("./private/")))
            .keystore(accessPrivate(new URI("./bucket/" + userName + "/").resolve("./keystore")))
            .inboxWithWriteAccess(accessPrivate(new URI("./bucket/" + userName + "/").resolve("./inbox/")))
            .build()
        );


        return auth;
    }

    private PublicResource access(URI path) {
        return new DefaultPublicResource(path);
    }

    private PrivateResource accessPrivate(URI path) {
        return new DefaultPrivateResource(path);
    }

    @SneakyThrows
    private Stream<PrivateResource> listFiles(ResourceLocation path) {
        return Files.walk(resolve(path.locationWithAccess(), false))
                .filter(it -> !it.startsWith("."))
                .filter(it -> !it.toFile().isDirectory())
                .map(it -> {
                    // since java on MacOS does not support meta info we will decode it from file name
                    String[] parts = it.toString().split("_");
                    return new PrivateResourceWithMeta(it.toUri(), parts[parts.length - 1]);
                });
    }

    @SneakyThrows
    private InputStream readFile(ResourceLocation path) {
        return MoreFiles.asByteSource(resolve(path.locationWithAccess(), false), StandardOpenOption.READ).openStream();
    }

    @SneakyThrows
    private OutputStream writeFile(ResourceLocation path) {
        Path filePath = resolve(path.locationWithAccess(), true);

        if (path instanceof PrivateResourceWithMeta) {
            // since java on MacOS does not support meta info we will encode it in file name
            filePath = Paths.get(
                    filePath.getParent().toString(),
                    filePath.getFileName().toString() + '_' + ((PrivateResourceWithMeta) path).metaInformation
            );
        }

        return MoreFiles.asByteSink(filePath, StandardOpenOption.CREATE).openStream();
    }

    private Path resolve(URI uri, boolean mkDirs) {
        Path path = Paths.get(tempDir.toUri().resolve(uri));
        if (!path.getParent().toFile().exists() && mkDirs) {
            path.getParent().toFile().mkdirs();
        }

        return Paths.get(tempDir.toUri().resolve(uri));
    }

    private static class PrivateResourceWithMeta extends DefaultPrivateResource {

        @Getter
        private final String metaInformation;

        PrivateResourceWithMeta(URI uri, String meta) {
            super(uri);
            this.metaInformation = meta;
        }

        @Override
        public Supplier<PrivateResource> applyRoot(ResourceLocation absolute) {
            return () -> new PrivateResourceWithMeta(
                    absolute.locationWithAccess().resolve(super.locationWithAccess()),
                    metaInformation
            );
        }
    }
}
