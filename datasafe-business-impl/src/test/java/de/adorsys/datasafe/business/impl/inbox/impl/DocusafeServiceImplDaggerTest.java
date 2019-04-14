package de.adorsys.datasafe.business.impl.inbox.impl;

import de.adorsys.datasafe.business.api.deployment.credentials.dto.SystemCredentials;
import de.adorsys.datasafe.business.api.deployment.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.file.FileIn;
import de.adorsys.datasafe.business.api.types.file.FileMeta;
import de.adorsys.datasafe.business.api.types.file.FileOut;
import de.adorsys.datasafe.business.api.types.inbox.InboxBucketPath;
import de.adorsys.datasafe.business.api.types.inbox.InboxReadRequest;
import de.adorsys.datasafe.business.api.types.inbox.InboxWriteRequest;
import de.adorsys.datasafe.business.api.types.privatespace.PrivateBucketPath;
import de.adorsys.datasafe.business.api.types.privatespace.PrivateReadRequest;
import de.adorsys.datasafe.business.api.types.privatespace.PrivateWriteRequest;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPublicProfile;
import de.adorsys.datasafe.business.impl.BaseMockitoTest;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDocusafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDocusafeServices;
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

        writeDataToPrivate(jane, "secret.txt", MESSAGE_ONE);

        PrivateBucketPath privateJane = getFirstFileInPrivate(jane);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane);

        sendToInbox(jane.getUserID(), john.getUserID(), "hello.txt", privateContentJane);

        InboxBucketPath inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn);
        assertThat(result).isEqualTo(MESSAGE_ONE);
    }

    private void writeDataToPrivate(UserIDAuth auth, String path, String data) {
        docusafeService.privateService().write(
            new PrivateWriteRequest(
                auth,
                new FileIn(new FileMeta(path), new ByteArrayInputStream(data.getBytes())))
        );
    }

    private PrivateBucketPath getFirstFileInPrivate(UserIDAuth inboxOwner) {
        List<PrivateBucketPath> files = docusafeService.privateService().list(inboxOwner).collect(Collectors.toList());
        log.info("{} has {} in PRIVATE", inboxOwner.getUserID().getValue(), files);
        return files.get(0);
    }

    private String readPrivateUsingPrivateKey(UserIDAuth user, PrivateBucketPath location) {
        FileOut out = new FileOut(
            new FileMeta(""),
            new ByteArrayOutputStream(1000)
        );

        docusafeService.privateService()
            .read(PrivateReadRequest.builder()
                .owner(user)
                .path(location)
                .response(out)
                .build()
            );

        String data = out.getData().toString();
        log.info("{} has {} in PRIVATE", user.getUserID().getValue(), data);

        return data;
    }

    private String readInboxUsingPrivateKey(UserIDAuth user, InboxBucketPath location) {
        FileOut out = new FileOut(
            new FileMeta(""),
            new ByteArrayOutputStream(1000)
        );

        docusafeService.inboxService()
            .read(InboxReadRequest.builder()
                .owner(user)
                .path(location)
                .response(out)
                .build()
            );

        String data = out.getData().toString();
        log.info("{} has {} in INBOX", user.getUserID().getValue(), data);

        return data;
    }

    private InboxBucketPath getFirstFileInInbox(UserIDAuth inboxOwner) {
        List<InboxBucketPath> files = docusafeService.inboxService().list(inboxOwner).collect(Collectors.toList());
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
