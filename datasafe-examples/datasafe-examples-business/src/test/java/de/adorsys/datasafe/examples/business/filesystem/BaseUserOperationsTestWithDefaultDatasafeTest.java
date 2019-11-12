package de.adorsys.datasafe.examples.business.filesystem;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * This test shows simplistic usage of Datasafe default services that reside on filesystem.
 */
class BaseUserOperationsTestWithDefaultDatasafeTest {

    private DefaultDatasafeServices defaultDatasafeServices;

    /**
     * This shows how you build Datasafe services. Note that you can override any class/module you want
     * by providing your own interface using {@link DefaultDatasafeServices} as a template.
     */
    @BeforeEach
    void createServices(@TempDir Path root) {
        // BEGIN_SNIPPET:Create Datasafe services
        // this will create all Datasafe files and user documents under <temp dir path>
        defaultDatasafeServices = DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"::toCharArray))
                .storage(new FileSystemStorageService(root))
                .build();
        // END_SNIPPET
    }

    /**
     * Creating new user is as simple as that:
     */
    @Test
    void registerUser() {
        // BEGIN_SNIPPET:Create new user
        // Creating new user with username 'user' and private/secret key password 'passwrd':
        /*
        IMPORTANT: For cases when user profile is stored on S3 without object locks, this requires some global
        synchronization due to eventual consistency or you need to supply globally unique username on registration
        */
        defaultDatasafeServices.userProfile().registerUsingDefaults(new UserIDAuth("user", "passwrd"::toCharArray));
        // END_SNIPPET

        assertThat(defaultDatasafeServices.userProfile().userExists(new UserID("user")));
    }

    /**
     * Writing file to private space is like that, you can check that it is encrypted:
     */
    @Test
    @SneakyThrows
    void writeFileToPrivateSpace() {
        // BEGIN_SNIPPET:Store file in privatespace
        // creating new user
        UserIDAuth user = registerUser("john");

        // writing string "Hello" to my/own/file.txt:
        // note that both resulting file content and its path are encrypted:
        try (OutputStream os = defaultDatasafeServices.privateService()
                .write(WriteRequest.forDefaultPrivate(user, "my/own/file.txt"))) {
            os.write("Hello".getBytes(StandardCharsets.UTF_8));
        }
        // END_SNIPPET

        assertThat(defaultDatasafeServices.privateService().list(ListRequest.forDefaultPrivate(user, ""))).hasSize(1);
    }

    /**
     * Reading file from private space:
     */
    @Test
    @SneakyThrows
    void readFileFromPrivateSpace() {
        // BEGIN_SNIPPET:Read file from privatespace
        // creating new user
        UserIDAuth user = registerUser("jane");

        // writing string "Hello Jane" to my/secret.txt into users' Jane privatespace:
        writeToPrivate(user, "my/secret.txt", "Hello Jane");

        byte[] helloJane;
        // reading encrypted data from my/secret.txt, note that path is also encrypted
        try (InputStream is = defaultDatasafeServices.privateService()
                .read(ReadRequest.forDefaultPrivate(user, "my/secret.txt"))) {
            helloJane = ByteStreams.toByteArray(is);
        }
        // END_SNIPPET

        // we've written "Hello Jane", so expecting that we read same
        assertThat(new String(helloJane)).isEqualTo("Hello Jane");
    }

    /**
     * Sending file with hello message to some other user:
     */
    @Test
    @SneakyThrows
    void shareWithJane() {
        // BEGIN_SNIPPET:Send file to INBOX
        // create Jane, so her INBOX does exist
        UserIDAuth jane = registerUser("jane");
        UserID janeUsername = new UserID("jane");

        // We send message "Hello John" to John just by his username
        try (OutputStream os = defaultDatasafeServices.inboxService()
                .write(WriteRequest.forDefaultPublic(Collections.singleton(janeUsername), "hello.txt"))) {
            os.write("Hello Jane".getBytes(StandardCharsets.UTF_8));
        }
        // END_SNIPPET

        assertThat(defaultDatasafeServices.inboxService().read(ReadRequest.forDefaultPrivate(jane, "hello.txt")))
                .hasContent("Hello Jane");
    }

    /**
     * Sending file with hello message to multiple some other users:
     */
    @Test
    @SneakyThrows
    void shareWithJaneAndJamie() {
        // BEGIN_SNIPPET:Send file to INBOX - multiple users
        // create Jane, so her INBOX does exist
        UserIDAuth jane = registerUser("jane");
        // create Jamie, so his INBOX does exist
        UserIDAuth jamie = registerUser("jamie");

        // We send message to both users by using their username:
        try (OutputStream os = defaultDatasafeServices.inboxService().write(
                WriteRequest.forDefaultPublic(ImmutableSet.of(jane.getUserID(), jamie.getUserID()), "hello.txt"))
        ) {
            os.write("Hello Jane and Jamie".getBytes(StandardCharsets.UTF_8));
        }
        // END_SNIPPET

        assertThat(defaultDatasafeServices.inboxService().read(ReadRequest.forDefaultPrivate(jane, "hello.txt")))
                .hasContent("Hello Jane and Jamie");
        assertThat(defaultDatasafeServices.inboxService().read(ReadRequest.forDefaultPrivate(jamie, "hello.txt")))
                .hasContent("Hello Jane and Jamie");
    }

    /**
     * This is how John can list his privatespace
     */
    @Test
    void listPrivate() {
        // BEGIN_SNIPPET:List privatespace
        // creating new user
        UserIDAuth user = registerUser("john");

        // let's create 3 files:
        writeToPrivate(user, "home/my/secret.txt", "secret");
        writeToPrivate(user, "home/watch/films.txt", "My favourite films");
        writeToPrivate(user, "home/hello.txt", "Hello");

        // Here's how to list private folder root
        List<AbsoluteLocation<ResolvedResource>> johnsPrivateFilesInRoot = defaultDatasafeServices.privateService()
                .list(ListRequest.forDefaultPrivate(user, "")).collect(Collectors.toList());
        // same files we created
        assertThat(johnsPrivateFilesInRoot)
                .extracting(it -> it.getResource().asPrivate().decryptedPath().asString())
                .containsExactlyInAnyOrder(
                        "home/my/secret.txt",
                        "home/watch/films.txt",
                        "home/hello.txt"
                );

        // Now let's list John's home/watch:
        List<AbsoluteLocation<ResolvedResource>> johnsPrivateFilesInWatch = defaultDatasafeServices.privateService()
                .list(ListRequest.forDefaultPrivate(user, "home/watch")).collect(Collectors.toList());
        // same files we created
        assertThat(johnsPrivateFilesInWatch)
                .extracting(it -> it.getResource().asPrivate().decryptedPath().asString())
                .containsExactly(
                        "home/watch/films.txt"
                );
        // END_SNIPPET
    }

    /**
     * This is how John can list his inbox
     */
    @Test
    void listInbox() {
        // BEGIN_SNIPPET:List INBOX
        // creating new user
        UserIDAuth user = registerUser("john");
        UserID johnUsername = new UserID("john");

        // let's share 2 messages:
        shareMessage(johnUsername, "home/my/secret.txt", "Hi there");
        shareMessage(johnUsername, "home/watch/films.txt", "Films you will like");

        // Here's how to list inbox folder root
        List<AbsoluteLocation<ResolvedResource>> johnsInboxFilesInRoot = defaultDatasafeServices.inboxService()
                .list(ListRequest.forDefaultPrivate(user, "")).collect(Collectors.toList());
        // same files we created, note that on filesystem file paths in INBOX are not encrypted
        assertThat(johnsInboxFilesInRoot)
                .extracting(it -> it.getResource().asPrivate().decryptedPath().toASCIIString())
                .containsExactlyInAnyOrder(
                        "home/my/secret.txt",
                        "home/watch/films.txt"
                );

        // Now let's list John's home/watch:
        List<AbsoluteLocation<ResolvedResource>> johnsInboxFilesInWatch = defaultDatasafeServices.inboxService()
                .list(ListRequest.forDefaultPrivate(user, "home/watch")).collect(Collectors.toList());
        // same files we created
        assertThat(johnsInboxFilesInWatch)
                .extracting(it -> it.getResource().asPrivate().decryptedPath().toASCIIString())
                .containsExactly(
                        "home/watch/films.txt"
                );
        // END_SNIPPET
    }

    /**
     * This is how John can read file from privatespace using list operation
     */
    @Test
    void readFromPrivate() {
        // BEGIN_SNIPPET:Read file from privatespace using list
        // creating new user
        UserIDAuth user = registerUser("john");

        // let's create 1 file:
        writeToPrivate(user, "home/my/secret.txt", "secret");

        List<AbsoluteLocation<ResolvedResource>> johnsPrivateFilesInMy = defaultDatasafeServices.privateService()
                .list(ListRequest.forDefaultPrivate(user, "home/my")).collect(Collectors.toList());

        // we have successfully read that file
        assertThat(defaultDatasafeServices.privateService().read(
                ReadRequest.forPrivate(user, johnsPrivateFilesInMy.get(0).getResource().asPrivate()))
        ).hasContent("secret");
        // END_SNIPPET
    }

    /**
     * This is how John can read file from inbox using list operation
     */
    @Test
    void readFromInbox() {
        // BEGIN_SNIPPET:Read file from INBOX
        // creating new user
        UserIDAuth user = registerUser("john");
        UserID johnUsername = new UserID("john");

        // let's create 1 file:
        shareMessage(johnUsername, "home/my/shared.txt", "shared message");

        // Lets list our INBOX
        List<AbsoluteLocation<ResolvedResource>> johnsInboxFilesInMy = defaultDatasafeServices.inboxService()
                .list(ListRequest.forDefaultPrivate(user, "")).collect(Collectors.toList());

        // we have successfully read that file
        assertThat(defaultDatasafeServices.inboxService().read(
                ReadRequest.forPrivate(user, johnsInboxFilesInMy.get(0).getResource().asPrivate()))
        ).hasContent("shared message");
        // END_SNIPPET
    }

    @SneakyThrows
    private void writeToPrivate(UserIDAuth user, String path, String fileContent) {
        try (OutputStream os = defaultDatasafeServices.privateService()
                .write(WriteRequest.forDefaultPrivate(user, path))) {
            os.write(fileContent.getBytes(StandardCharsets.UTF_8));
        }
    }

    private UserIDAuth registerUser(String username) {
        UserIDAuth creds = new UserIDAuth(username, ReadKeyPasswordTestFactory.getForString("passwrd" + username));
        defaultDatasafeServices.userProfile().registerUsingDefaults(creds);
        return creds;
    }

    @SneakyThrows
    private void shareMessage(UserID forUser, String messageName, String message) {
        try (OutputStream os = defaultDatasafeServices.inboxService()
                .write(WriteRequest.forDefaultPublic(Collections.singleton(forUser), messageName))) {
            os.write(message.getBytes(StandardCharsets.UTF_8));
        }
    }
}
