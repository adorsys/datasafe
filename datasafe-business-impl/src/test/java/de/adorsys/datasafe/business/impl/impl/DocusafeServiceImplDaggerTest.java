package de.adorsys.datasafe.business.impl.impl;

import com.google.common.io.ByteStreams;
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
import de.adorsys.datasafe.business.impl.BaseMockitoTest;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDocusafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDocusafeServices;
import de.adorsys.datasafe.business.impl.types.DefaultPrivateResource;
import de.adorsys.datasafe.business.impl.types.DefaultPublicResource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DocusafeServiceImplDaggerTest extends BaseMockitoTest {

    private static final String MESSAGE_ONE = "Hello here";

    private DefaultDocusafeServices docusafeService = DaggerDefaultDocusafeServices
            .builder()
            .build();

    private UserIDAuth john;
    private UserIDAuth jane;

    @Test
    void testWriteToPrivateListPrivateReadPrivateAndSendToAndReadFromInbox(@TempDir Path dfsLocation) {
        System.setProperty("SC-FILESYSTEM", dfsLocation.toFile().getAbsolutePath());

        registerJohnAndJane();

        writeDataToPrivate(jane, "./secret.txt", MESSAGE_ONE);

        PrivateResource privateJane = getFirstFileInPrivate(jane);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane);

        sendToInbox(jane.getUserID(), john.getUserID(), "./hello.txt", privateContentJane);

        PrivateResource inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn);
        assertThat(result).isEqualTo(MESSAGE_ONE);
    }

    @SneakyThrows
    private void writeDataToPrivate(UserIDAuth auth, String path, String data) {
        OutputStream stream = docusafeService.privateService().write(
                WriteRequest.<UserIDAuth>builder()
                        .owner(auth)
                        .location(new DefaultPrivateResource(new URI(path)))
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
                        .location(new DefaultPrivateResource(new URI("./")))
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
                .location(new DefaultPrivateResource(new URI("./")))
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
            WriteRequest.<UserID>builder()
                    .location(new DefaultPrivateResource(new URI("./" + filename)))
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
            .inbox(access(new URI("s3://bucket/" + userName + "/").resolve("./inbox/")))
            .publicKeys(access(new URI("s3://bucket/" + userName + "/").resolve("./keystore")))
            .build()
        );

        docusafeService.userProfile().registerPrivate(CreateUserPrivateProfile.builder()
            .id(auth)
            .privateStorage(accessPrivate(new URI("s3://bucket/" + userName + "/").resolve("./private/")))
            .keystore(accessPrivate(new URI("s3://bucket/" + userName + "/").resolve("./keystore")))
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
}
