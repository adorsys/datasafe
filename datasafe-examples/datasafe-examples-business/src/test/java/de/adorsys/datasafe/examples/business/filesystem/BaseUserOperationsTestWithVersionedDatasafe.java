package de.adorsys.datasafe.examples.business.filesystem;

import de.adorsys.datasafe.business.impl.service.DaggerVersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * This test shows simplistic usage of Datasafe versioned services that reside on filesystem.
 */
class BaseUserOperationsTestWithVersionedDatasafe {

    private VersionedDatasafeServices versionedServices;

    /**
     * This shows how you build Software-versioned Datasafe services. Note that you can override any class/module you
     * want by providing your own interface using {@link VersionedDatasafeServices} as a template.
     */
    @BeforeEach
    void createServices(@TempDir Path root) {
        // this will create all Datasafe files and user documents under <temp dir path>
        versionedServices = DaggerVersionedDatasafeServices.builder()
                .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"))
                .storage(new FileSystemStorageService(root))
                .build();
    }

    /**
     * Creating new user is same
     */
    @Test
    void registerUser() {
        // Creating new user:
        versionedServices.userProfile().registerUsingDefaults(new UserIDAuth("user", "passwrd"));

        assertThat(versionedServices.userProfile().userExists(new UserID("user")));
    }


    /**
     * Writing file - you can write it to versioned private space multiple times and you will see only latest
     */
    @Test
    @SneakyThrows
    void writeFileToVersionedPrivateSpace() {
        // creating new user
        UserIDAuth user = registerUser("john");

        // writing string "Hello" to my/own/file.txt 3 times:
        // note that both resulting file content and its path are encrypted:
        for (int i = 1; i <= 3; ++i) {
            try (OutputStream os = versionedServices.latestPrivate()
                    .write(WriteRequest.forDefaultPrivate(user, "my/own/file.txt"))) {
                os.write(("Hello " + i).getBytes(StandardCharsets.UTF_8));
            }
        }

        // and still we read only latest file
        assertThat(versionedServices.latestPrivate()
                .read(ReadRequest.forDefaultPrivate(user, "my/own/file.txt"))
        ).hasContent("Hello 3");
        // and still only one file visible on latest view
        assertThat(versionedServices.latestPrivate().list(ListRequest.forDefaultPrivate(user, ""))).hasSize(1);
        // but there are 3 versions of file stored:
        assertThat(versionedServices.versionInfo().versionsOf(
                ListRequest.forDefaultPrivate(user, "my/own/file.txt"))
        ).hasSize(3);
    }

    private UserIDAuth registerUser(String username) {
        UserIDAuth creds = new UserIDAuth(username, "passwrd" + username);
        versionedServices.userProfile().registerUsingDefaults(creds);
        return creds;
    }
}
