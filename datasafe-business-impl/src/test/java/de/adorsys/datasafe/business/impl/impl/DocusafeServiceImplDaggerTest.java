package de.adorsys.datasafe.business.impl.impl;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.SystemCredentials;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.file.FileIn;
import de.adorsys.datasafe.business.api.types.file.FileOut;
import de.adorsys.datasafe.business.api.types.inbox.InboxReadRequest;
import de.adorsys.datasafe.business.api.types.inbox.InboxWriteRequest;
import de.adorsys.datasafe.business.api.types.privatespace.PrivateReadRequest;
import de.adorsys.datasafe.business.api.types.privatespace.PrivateWriteRequest;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPublicProfile;
import de.adorsys.datasafe.business.impl.BaseMockitoTest;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDocusafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDocusafeServices;
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

import static de.adorsys.datasafe.business.impl.profile.DFSSystem.CREDS_ID;
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

        URI privateJane = getFirstFileInPrivate(jane);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane);

        sendToInbox(jane.getUserID(), john.getUserID(), "./hello.txt", privateContentJane);

        URI inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn);
        assertThat(result).isEqualTo(MESSAGE_ONE);
    }

    @SneakyThrows
    private void writeDataToPrivate(UserIDAuth auth, String path, String data) {
        OutputStream stream = docusafeService.privateService().write(
            new PrivateWriteRequest(
                auth,
                new FileIn(new URI(path)))
        );

        stream.write(data.getBytes());
        stream.close();
    }

    @SneakyThrows
    private URI getFirstFileInPrivate(UserIDAuth inboxOwner) {
        List<URI> files = docusafeService.privateService().list(inboxOwner).collect(Collectors.toList());
        log.info("{} has {} in PRIVATE", inboxOwner.getUserID().getValue(), files);
        return files.get(0);
    }

    @SneakyThrows
    private String readPrivateUsingPrivateKey(UserIDAuth user, URI location) {
        FileOut out = new FileOut(
            new URI(""));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream dataStream = docusafeService.privateService()
            .read(PrivateReadRequest.builder()
                .owner(user)
                .path(location)
                .response(out)
                .build()
            );

        ByteStreams.copy(dataStream, outputStream);
        String data = new String(outputStream.toByteArray());
        log.info("{} has {} in PRIVATE", user.getUserID().getValue(), data);

        return data;
    }

    @SneakyThrows
    private String readInboxUsingPrivateKey(UserIDAuth user, URI location) {
        FileOut out = new FileOut(
            new URI("")
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream dataStream = docusafeService.inboxService()
            .read(InboxReadRequest.builder()
                .owner(user)
                .path(location)
                .response(out)
                .build()
            );

        ByteStreams.copy(dataStream, outputStream);
        String data = new String(outputStream.toByteArray());
        log.info("{} has {} in INBOX", user.getUserID().getValue(), data);

        return data;
    }

    private URI getFirstFileInInbox(UserIDAuth inboxOwner) {
        List<URI> files = docusafeService.inboxService().list(inboxOwner).collect(Collectors.toList());
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
            new InboxWriteRequest(
                from,
                to,
                new FileIn(new URI(filename))
            )
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
            .privateStorage(access(new URI("s3://bucket/" + userName + "/").resolve("./private/")))
            .keystore(access(new URI("s3://bucket/" + userName + "/").resolve("./keystore")))
            .build()
        );


        return auth;
    }

    private DFSAccess access(URI path) {
        return DFSAccess.builder()
            .physicalPath(path)
            .logicalPath(path)
            .credentials(SystemCredentials.builder().id(CREDS_ID).build())
            .build();
    }
}
