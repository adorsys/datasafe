package de.adorsys.datasafe.business.impl.inbox.impl;

import de.adorsys.datasafe.business.api.deployment.credentials.dto.SystemCredentials;
import de.adorsys.datasafe.business.api.deployment.inbox.dto.InboxReadRequest;
import de.adorsys.datasafe.business.api.deployment.inbox.dto.InboxWriteRequest;
import de.adorsys.datasafe.business.api.deployment.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.InboxBucketPath;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.file.FileIn;
import de.adorsys.datasafe.business.api.types.file.FileMeta;
import de.adorsys.datasafe.business.api.types.file.FileOnBucket;
import de.adorsys.datasafe.business.api.types.file.FileOut;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPublicProfile;
import de.adorsys.datasafe.business.impl.BaseMockitoTest;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDocusafeService;
import de.adorsys.datasafe.business.impl.service.DefaultDocusafeService;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.business.impl.profile.DFSSystem.CREDS_ID;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class InboxServiceImplDaggerTest extends BaseMockitoTest {

    private static final String MESSAGE_ONE = "Hello here";

    private DefaultDocusafeService docusafeService = DaggerDefaultDocusafeService
            .builder()
            .build();

    private UserIDAuth john;
    private UserIDAuth jane;

    @Test
    void testDaggerObjectCreation(@TempDir Path dfsLocation) {
        System.setProperty("SC-FILESYSTEM", dfsLocation.toFile().getAbsolutePath());

        registerJohnAndJane();

        sendToInbox(jane.getUserID(), john.getUserID(), "hello.txt", MESSAGE_ONE);
        FileOnBucket inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn);

        assertThat(result).isEqualTo(MESSAGE_ONE);
    }

    private String readInboxUsingPrivateKey(UserIDAuth user, FileOnBucket location) {
        FileOut out = new FileOut(
            new FileMeta(""),
            new ByteArrayOutputStream(1000)
        );

        docusafeService.inboxService()
            .read(InboxReadRequest.builder()
                .owner(user)
                .path(new InboxBucketPath(location.getPath()))
                .response(out)
                .build()
            );

        String data = out.getData().toString();
        log.info("{} has {} in INBOX", user.getUserID().getValue(), data);

        return data;
    }

    private FileOnBucket getFirstFileInInbox(UserIDAuth inboxOwner) {
        List<FileOnBucket> files = docusafeService.inboxService().list(inboxOwner).collect(Collectors.toList());
        log.info("{} has {} in INBOX", inboxOwner.getUserID().getValue(), files);
        return files.get(0);
    }

    private void registerJohnAndJane() {
        john = registerUser("john");
        jane = registerUser("jane");
    }

    private void sendToInbox(UserID from, UserID to, String filename, String data) {
        docusafeService.inboxService().write(
            new InboxWriteRequest(
                from,
                to,
                new FileIn(new FileMeta(filename), new ByteArrayInputStream(data.getBytes())))
        );
    }

    private UserIDAuth registerUser(String userName) {
        UserIDAuth auth = new UserIDAuth();
        auth.setUserID(new UserID(userName));
        auth.setReadKeyPassword(new ReadKeyPassword("secure-password " + userName));

        docusafeService.userProfile().registerPublic(CreateUserPublicProfile.builder()
            .id(auth.getUserID())
            .inbox(access(new BucketPath(userName).append("inbox")))
            .publicKeys(access(new BucketPath(userName).append("keystore")))
            .build()
        );

        docusafeService.userProfile().registerPrivate(CreateUserPrivateProfile.builder()
            .id(auth)
            .privateStorage(access(new BucketPath(userName).append("private")))
            .keystore(access(new BucketPath(userName).append("keystore")))
            .build()
        );


        return auth;
    }

    private DFSAccess access(BucketPath path) {
        return DFSAccess.builder()
            .physicalPath(path)
            .logicalPath(path)
            .credentials(SystemCredentials.builder().id(CREDS_ID).build())
            .build();
    }
}
