package de.adorsys.datasafe.business.impl.testcontainers;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.deployment.credentials.dto.SystemCredentials;
import de.adorsys.datasafe.business.api.deployment.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.DFSAccess;
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
import de.adorsys.datasafe.business.impl.privatestore.impl.PrivateSpaceServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.business.impl.profile.DFSSystem.CREDS_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@Slf4j
public class TestContainerTest {

    private TestDocusafeServices docusafeService = DaggerTestDocusafeServices
            .builder()
            .build();

    private static final String dockerComposePath = "src/test/resources/docker-compose.yml";
    private static DockerComposeContainer compose = new DockerComposeContainer(new File(dockerComposePath))
            .withExposedService("minio", 9000, Wait.forListeningPort());

    @BeforeAll
    static void beforeAll() {
        compose.start();
    }

    @BeforeEach
    void setup() {
        System.setProperty("SC-AMAZONS3", "http://127.0.0.1:9000,admin,password,us-east-1,home");
    }

    @Test
    @SneakyThrows
    public void savePrivateFileTest() {

        UserIDAuth john = registerUser("john");

        String originalMessage = "Hello here";

        // write to private
//        URI path = new URI("./path/to/file.txt");
        URI path = new URI("./file.txt");
        PrivateWriteRequest writeRequest = new PrivateWriteRequest(john, new FileIn(path));
        PrivateSpaceServiceImpl privateSpaceService = docusafeService.privateService();
        OutputStream stream = privateSpaceService.write(writeRequest);
        stream.write(originalMessage.getBytes());
        stream.close();

        verify(docusafeService.pathEncryption()).encrypt(any(), any());

        // read from private
        List<URI> files = privateSpaceService.list(john).collect(Collectors.toList());
        log.info("{} has {} in PRIVATE", john.getUserID().getValue(), files);
        URI location = files.get(0);

        FileOut out = new FileOut(new URI(""));
        PrivateReadRequest readRequest = PrivateReadRequest.builder().owner(john).path(location).response(out).build();
        InputStream dataStream = privateSpaceService.read(readRequest);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteStreams.copy(dataStream, outputStream);
        String readMessage = new String(outputStream.toByteArray());
        log.info("{} has {} in PRIVATE", john.getUserID().getValue(), readMessage);

        assertThat(readMessage).isEqualTo(originalMessage);
    }

    @Test
    @SneakyThrows
    public void sendToInboxTest() {
        UserIDAuth from = registerUser("sender");
        UserIDAuth to = registerUser("recipient");

        String originalMessage = "Hello here";

        // write to inbox
//        URI path = new URI("./sub/dir/hello.txt");
        URI path = new URI("./hello.txt");
        InboxWriteRequest inboxWriteRequest = new InboxWriteRequest(from.getUserID(), to.getUserID(), new FileIn(path));
        OutputStream stream = docusafeService.inboxService().write(inboxWriteRequest);

        stream.write(originalMessage.getBytes());
        stream.close();

        List<URI> files = docusafeService.inboxService().list(to).collect(Collectors.toList());
        log.info("{} has {} in INBOX", to.getUserID().getValue(), files);
        URI location = files.get(0);

        // read inbox
        FileOut out = new FileOut(new URI(""));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InboxReadRequest inboxReadRequest = InboxReadRequest.builder().owner(to).path(location).response(out).build();
        InputStream dataStream = docusafeService.inboxService().read(inboxReadRequest);

        ByteStreams.copy(dataStream, outputStream);
        String receivedMessage =  new String(outputStream.toByteArray());
        log.info("{} has {} in INBOX", to.getUserID().getValue(), receivedMessage);

        assertThat(originalMessage).isEqualTo(receivedMessage);
    }

    @SneakyThrows
    private UserIDAuth registerUser(String userName) {
        UserIDAuth auth = new UserIDAuth();
        auth.setUserID(new UserID(userName));
        auth.setReadKeyPassword(new ReadKeyPassword("secure-password " + userName));

        URI uri = new URI("s3://bucket/" + userName + "/");
        DFSAccess keyStoreAccess = access(uri.resolve("./keystore"));
        CreateUserPublicProfile userPublicProfile = CreateUserPublicProfile.builder()
                .id(auth.getUserID())
                .inbox(access(uri.resolve("./inbox/")))
                .publicKeys(keyStoreAccess)
                .build();
        docusafeService.userProfile().registerPublic(userPublicProfile);

        CreateUserPrivateProfile userPrivateProfile = CreateUserPrivateProfile.builder()
                .id(auth)
                .privateStorage(access(uri.resolve("./private/")))
                .keystore(keyStoreAccess)
                .build();
        docusafeService.userProfile().registerPrivate(userPrivateProfile);

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
