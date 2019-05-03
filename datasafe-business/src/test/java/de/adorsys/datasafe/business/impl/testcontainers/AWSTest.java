package de.adorsys.datasafe.business.impl.testcontainers;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.DefaultPublicResource;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceServiceImpl;
import de.adorsys.datasafe.business.impl.storage.S3StorageListService;
import de.adorsys.datasafe.business.impl.storage.S3StorageReadService;
import de.adorsys.datasafe.business.impl.storage.S3StorageWriteService;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@Slf4j
class AWSTest {

    private String accessKeyID = "secret";
    private String secretAccessKey = "secret";
    private String region = "eu-central-1";
    private String bucketName = "adorsys-docusafe";
    private BasicAWSCredentials creds = new BasicAWSCredentials(accessKeyID, secretAccessKey);
    private AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(creds))
            .withRegion(region)
            .build();

    private TestDocusafeServices docusafeService = DaggerTestDocusafeServices.builder()
            .storageList(new S3StorageListService(s3, bucketName))
            .storageRead(new S3StorageReadService(s3, bucketName))
            .storageWrite(new S3StorageWriteService(s3, bucketName))
            .build();

    @Test
    @SneakyThrows
    void savePrivateFileTest() {

        UserIDAuth john = registerUser("johnny" + System.currentTimeMillis());

        String originalMessage = "Hello here";

        // write to private
        URI path = new URI("./folder1/file.txt");
        PrivateResource privateResource = DefaultPrivateResource.forPrivate(path);
        WriteRequest<UserIDAuth, PrivateResource> writeRequest = WriteRequest.<UserIDAuth, PrivateResource>builder()
                .owner(john)
                .location(privateResource)
                .build();
        PrivateSpaceServiceImpl privateSpaceService = docusafeService.privateService();
        OutputStream stream = privateSpaceService.write(writeRequest);
        stream.write(originalMessage.getBytes());
        stream.close();

        verify(docusafeService.pathEncryption()).encrypt(any(), any());

        // read from private
        ListRequest<UserIDAuth> listRequest = ListRequest.<UserIDAuth>builder()
                .owner(john)
                .location(DefaultPrivateResource.ROOT)
                .build();
        List<PrivateResource> files = docusafeService.privateService().list(listRequest).collect(Collectors.toList());
        log.info("{} has {} in PRIVATE", john.getUserID().getValue(), files);
        PrivateResource location = files.get(0);

        ReadRequest<UserIDAuth> readRequest = ReadRequest.<UserIDAuth>builder()
                .owner(john)
                .location(location)
                .build();
        InputStream dataStream = privateSpaceService.read(readRequest);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteStreams.copy(dataStream, outputStream);
        String readMessage = new String(outputStream.toByteArray());
        log.info("{} has {} in PRIVATE", john.getUserID().getValue(), readMessage);

        assertThat(readMessage).isEqualTo(originalMessage);
    }

    @Test
    @SneakyThrows
    void sendToInboxTest() {
        UserIDAuth to = registerUser("recipient");

        String originalMessage = "Hello here";

        // write to inbox
        URI path = new URI("./hello.txt");
        DefaultPublicResource location = new DefaultPublicResource(path);
        WriteRequest<UserID, PublicResource> writeRequest = WriteRequest.<UserID, PublicResource>builder()
                .owner(to.getUserID())
                .location(location)
                .build();
        OutputStream stream = docusafeService.inboxService().write(writeRequest);
        stream.write(originalMessage.getBytes());
        stream.close();

        // list inbox
        ListRequest<UserIDAuth> listRequest = ListRequest.<UserIDAuth>builder()
                .owner(to)
                .location(DefaultPrivateResource.ROOT)
                .build();
        List<PrivateResource> files = docusafeService.inboxService().list(listRequest).collect(Collectors.toList());
        log.info("{} has {} in INBOX", to.getUserID().getValue(), files);
        PrivateResource privateResource = files.get(0);

        // read inbox
        ReadRequest<UserIDAuth> readRequest = ReadRequest.<UserIDAuth>builder()
                .owner(to)
                .location(privateResource)
                .build();
        InputStream dataStream = docusafeService.inboxService().read(readRequest);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
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

        CreateUserPublicProfile userPublicProfile = CreateUserPublicProfile.builder()
                .id(auth.getUserID())
                .inbox(access(new URI("./bucket/" + userName + "/").resolve("./inbox/")))
                .publicKeys(access(new URI("./bucket/" + userName + "/").resolve("./keystore")))
                .build();
        docusafeService.userProfile().registerPublic(userPublicProfile);

        CreateUserPrivateProfile userPrivateProfile = CreateUserPrivateProfile.builder()
                .id(auth)
                .privateStorage(accessPrivate(new URI("./bucket/" + userName + "/").resolve("./private/")))
                .keystore(accessPrivate(new URI("./bucket/" + userName + "/").resolve("./keystore")))
                .inboxWithWriteAccess(accessPrivate(new URI("./bucket/" + userName + "/").resolve("./inbox/")))
                .build();
        docusafeService.userProfile().registerPrivate(userPrivateProfile);

        return auth;
    }

    private PublicResource access(URI path) {
        return new DefaultPublicResource(path);
    }

    private PrivateResource accessPrivate(URI path) {
        return new DefaultPrivateResource(path, URI.create(""), URI.create(""));
    }
}
