package de.adorsys.datasafe.examples.business.filesystem;

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
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * This test shows simplistic usage of Datasafe default services that reside on filesystem.
 */
class BaseUserOperationsTestWithDefaultDatasafe {

    private DefaultDatasafeServices defaultDatasafeServices;

    /**
     * This shows how you build Datasafe services. Note that you can override any class/module you want
     * by providing your own interface using {@link DefaultDatasafeServices} as a template.
     */
    @BeforeEach
    void createServices(@TempDir Path root) {
        // this will create all Datasafe files and user documents under <temp dir path>
        defaultDatasafeServices = DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"))
                .storage(new FileSystemStorageService(root))
                .build();
    }

    /**
     * Creating new user is as simple as that:
     */
    @Test
    void registerUser() {
        // Creating new user:
        defaultDatasafeServices.userProfile().registerUsingDefaults(new UserIDAuth("user", "passwrd"));

        assertThat(defaultDatasafeServices.userProfile().userExists(new UserID("user")));
    }

    /**
     * Writing file to private space is like that, you can check that it is encrypted:
     */
    @Test
    @SneakyThrows
    void writeFileToPrivateSpace() {
        // creating new user
        UserIDAuth user = registerUser("john");

        // writing string "Hello" to /my/own/file.txt:
        // note that both resulting file content and its path are encrypted:
        try (OutputStream os = defaultDatasafeServices.privateService()
                .write(WriteRequest.forDefaultPrivate(user, "/my/own/file.txt"))) {
            os.write("Hello".getBytes(StandardCharsets.UTF_8));
        }

        assertThat(defaultDatasafeServices.privateService().list(ListRequest.forDefaultPrivate(user, ""))).hasSize(1);
    }

    /**
     * Reading file from private space:
     */
    @Test
    @SneakyThrows
    void readFileFromPrivateSpace() {
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

        // we've written "Hello Jane", so expecting that we read same
        assertThat(new String(helloJane)).isEqualTo("Hello Jane");
    }

    /**
     * Sending file with hello message to some other user:
     */
    @Test
    @SneakyThrows
    void shareWithJohn() {
        // create John, so his INBOX does exist
        UserIDAuth john = registerUser("john");
        UserID johnUsername = new UserID("john");

        // We send message "Hello John" to John just by his username
        try (OutputStream os = defaultDatasafeServices.inboxService()
                .write(WriteRequest.forDefaultPublic(johnUsername, "hello.txt"))) {
            os.write("Hello John".getBytes(StandardCharsets.UTF_8));
        }

        assertThat(defaultDatasafeServices.inboxService().read(ReadRequest.forDefaultPrivate(john, "hello.txt")))
                .hasContent("Hello John");
    }

    /**
     * This is how John can list his privatespace
     */
    @Test
    void listPrivate() {
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
                .extracting(it -> it.getResource().asPrivate().decryptedPath().toASCIIString())
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
                .extracting(it -> it.getResource().asPrivate().decryptedPath().toASCIIString())
                .containsExactly(
                        "home/watch/films.txt"
                );
    }

    /**
     * This is how John can list his inbox
     */

    @SneakyThrows
    private void writeToPrivate(UserIDAuth user, String path, String fileContent) {
        try (OutputStream os = defaultDatasafeServices.privateService()
                .write(WriteRequest.forDefaultPrivate(user, path))) {
            os.write(fileContent.getBytes(StandardCharsets.UTF_8));
        }
    }

    private UserIDAuth registerUser(String username) {
        UserIDAuth creds = new UserIDAuth(username, "passwrd" + username);
        defaultDatasafeServices.userProfile().registerUsingDefaults(creds);
        return creds;
    }

    @SneakyThrows
    private void shareMessage(UserID forUser, String messageName, String message) {
        try (OutputStream os = defaultDatasafeServices.inboxService()
                .write(WriteRequest.forDefaultPublic(forUser, messageName))) {
            os.write(message.getBytes(StandardCharsets.UTF_8));
        }
    }
}
