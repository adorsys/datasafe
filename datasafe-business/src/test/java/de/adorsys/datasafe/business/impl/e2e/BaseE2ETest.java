package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.DefaultPublicResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.impl.BaseMockitoTest;
import de.adorsys.datasafe.business.impl.service.DefaultDocusafeServices;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseE2ETest extends BaseMockitoTest {

    protected static final String PRIVATE_COMPONENT = "private";
    protected static final String PRIVATE_FILES_COMPONENT = PRIVATE_COMPONENT + "/files";
    protected static final String INBOX_COMPONENT = "inbox";

    protected DefaultDocusafeServices services;

    protected UserIDAuth john;
    protected UserIDAuth jane;

    @SneakyThrows
    protected void writeDataToPrivate(UserIDAuth auth, String path, String data) {
        OutputStream stream = services.privateService().write(WriteRequest.forPrivate(auth, path));
        stream.write(data.getBytes());
        stream.close();
    }

    protected PrivateResource getFirstFileInPrivate(UserIDAuth inboxOwner) {
        List<PrivateResource> files = services.privateService().list(new ListRequest<>(inboxOwner, "./"))
                .collect(Collectors.toList());

        log.info("{} has {} in PRIVATE", inboxOwner.getUserID().getValue(), files);
        return files.get(0);
    }

    @SneakyThrows
    protected String readPrivateUsingPrivateKey(UserIDAuth user, PrivateResource location) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream dataStream = services.privateService().read(new ReadRequest<>(user, location));

        ByteStreams.copy(dataStream, outputStream);
        String data = new String(outputStream.toByteArray());
        log.info("{} has {} in PRIVATE", user.getUserID().getValue(), data);

        return data;
    }

    @SneakyThrows
    protected String readInboxUsingPrivateKey(UserIDAuth user, PrivateResource location) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream dataStream = services.inboxService().read(new ReadRequest<>(user, location));

        ByteStreams.copy(dataStream, outputStream);
        String data = new String(outputStream.toByteArray());
        log.info("{} has {} in INBOX", user.getUserID().getValue(), data);

        return data;
    }

    protected PrivateResource getFirstFileInInbox(UserIDAuth inboxOwner) {
        List<PrivateResource> files = services.inboxService().list(new ListRequest<>(inboxOwner, "./"))
                .collect(Collectors.toList());

        log.info("{} has {} in INBOX", inboxOwner.getUserID().getValue(), files);
        return files.get(0);
    }

    protected void registerJohnAndJane(URI rootLocation) {
        john = registerUser("john", rootLocation);
        jane = registerUser("jane", rootLocation);
    }

    @SneakyThrows
    protected void sendToInbox(UserID from, UserID to, String filename, String data) {
        OutputStream stream = services.inboxService().write(WriteRequest.forPublic(to, "./" + filename));
        stream.write(data.getBytes());
        stream.close();
    }

    protected UserIDAuth registerUser(String userName, URI rootLocation) {
        UserIDAuth auth = new UserIDAuth();
        auth.setUserID(new UserID(userName));
        auth.setReadKeyPassword(new ReadKeyPassword("secure-password " + userName));

        rootLocation = rootLocation.resolve(userName + "/");

        services.userProfile().registerPublic(CreateUserPublicProfile.builder()
                .id(auth.getUserID())
                .inbox(access(rootLocation.resolve("./" + INBOX_COMPONENT + "/")))
                .publicKeys(access(rootLocation.resolve("./"+ PRIVATE_COMPONENT + "/keystore")))
                .build()
        );

        services.userProfile().registerPrivate(CreateUserPrivateProfile.builder()
                .id(auth)
                .privateStorage(accessPrivate(rootLocation.resolve("./" + PRIVATE_FILES_COMPONENT + "/")))
                .keystore(accessPrivate(rootLocation.resolve("./"+ PRIVATE_COMPONENT + "/keystore")))
                .inboxWithWriteAccess(accessPrivate(rootLocation.resolve("./" + INBOX_COMPONENT + "/")))
                .build()
        );

        return auth;
    }

    protected void removeUser(UserIDAuth userIDAuth) {
        services.userProfile().deregister(userIDAuth);
        log.info("user deleted: {}", userIDAuth.getUserID().getValue());
    }

    private PublicResource access(URI path) {
        return new DefaultPublicResource(path);
    }

    private PrivateResource accessPrivate(URI path) {
        return new DefaultPrivateResource(path, URI.create(""), URI.create(""));
    }
}
