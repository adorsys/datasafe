package de.adorsys.datasafe.examples.business.filesystem;

import de.adorsys.datasafe.business.impl.service.DaggerVersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.metainfo.version.impl.version.types.DFSVersion;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Versioned;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * This test shows simplistic usage of Datasafe versioned services that reside on filesystem.
 */
class BaseUserOperationsTestWithVersionedDatasafeTest {

    private VersionedDatasafeServices versionedServices;

    /**
     * This shows how you build Software-versioned Datasafe services. Note that you can override any class/module you
     * want by providing your own interface using {@link VersionedDatasafeServices} as a template.
     */
    @BeforeEach
    void createServices(@TempDir Path root) {
        // BEGIN_SNIPPET:Create versioned Datasafe services
        // this will create all Datasafe files and user documents under <temp dir path>
        versionedServices = DaggerVersionedDatasafeServices.builder()
                .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"::toCharArray))
                .storage(new FileSystemStorageService(root))
                .build();
        // END_SNIPPET
    }

    /**
     * Creating new user is same
     */
    @Test
    void registerUser() {
        // BEGIN_SNIPPET:Creating user for versioned services looks same
        // Creating new user:
        /*
        IMPORTANT: For cases when user profile is stored on S3 without object locks, this requires some global
        synchronization due to eventual consistency or you need to supply globally unique username on registration
        */
        versionedServices.userProfile().registerUsingDefaults(new UserIDAuth("user", "passwrd"::toCharArray));
        // END_SNIPPET

        assertThat(versionedServices.userProfile().userExists(new UserID("user")));
    }


    /**
     * Writing file - you can write it to versioned private space multiple times and you will see only latest
     */
    @Test
    @SneakyThrows
    void writeFileToVersionedPrivateSpace() {
        // BEGIN_SNIPPET:Saving file couple of times - versioned
        // creating new user
        UserIDAuth user = registerUser("john");

        // writing string "Hello " + index to my/own/file.txt 3 times:
        // note that both resulting file content and its path are encrypted:
        for (int i = 1; i <= 3; ++i) {
            try (OutputStream os = versionedServices.latestPrivate()
                    .write(WriteRequest.forDefaultPrivate(user, "my/own/file.txt"))) {
                os.write(("Hello " + i).getBytes(StandardCharsets.UTF_8));
                Thread.sleep(1000L); // this will change file modified dates
            }
        }

        // and still we read only latest file
        assertThat(versionedServices.latestPrivate()
                .read(ReadRequest.forDefaultPrivate(user, "my/own/file.txt"))
        ).hasContent("Hello 3");
        // but there are 3 versions of file stored physically in users' privatespace:
        assertThat(versionedServices.privateService().list(
            ListRequest.forDefaultPrivate(user, "my/own/file.txt"))
        ).hasSize(3);
        // and still only one file visible on latest view
        assertThat(versionedServices.latestPrivate().list(ListRequest.forDefaultPrivate(user, ""))).hasSize(1);
        // END_SNIPPET

        // BEGIN_SNIPPET:Lets check how to read oldest file version
        // so lets collect all versions
        List<Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion>> withVersions =
            versionedServices.versionInfo().versionsOf(
                ListRequest.forDefaultPrivate(user, "my/own/file.txt")
            ).collect(Collectors.toList());
        // so that we can find oldest
        Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion> oldest =
            withVersions.stream()
                .sorted(Comparator.comparing(it -> it.absolute().getResource().getModifiedAt()))
                .collect(Collectors.toList())
                .get(0);
        // and read oldest content
        assertThat(versionedServices.privateService()
            .read(ReadRequest.forPrivate(user, oldest.absolute().getResource().asPrivate()))
        ).hasContent("Hello 1");
        // END_SNIPPET
    }

    /**
     * Imagine the usecase when you have some cached local files from users' privatespace and your application
     * wants to check if your local version is outdated and you need to download new version from storage.
     */
    @Test
    @SneakyThrows
    void checkThatWeNeedToDownloadNewFile() {
        // BEGIN_SNIPPET:Check if we have latest file locally
        // creating new user
        UserIDAuth user = registerUser("john");

        // First lets store some file, for example John stored it from mobile phone
        try (OutputStream os = versionedServices.latestPrivate()
                .write(WriteRequest.forDefaultPrivate(user, "my/own/file.txt"))) {
            os.write(("Hello old version").getBytes(StandardCharsets.UTF_8));
        }

        // Application on mobile phone caches file content to improve performance, so it should cache timestamp too
        Instant savedOnMobile = versionedServices.latestPrivate()
                .list(ListRequest.forDefaultPrivate(user, "my/own/file.txt"))
                .findAny().get().getResource().getModifiedAt();

        // Now John uses PC to write data to my/own/file.txt with some updated data
        Thread.sleep(1000L); // it took some time for him to get to PC
        try (OutputStream os = versionedServices.latestPrivate()
                .write(WriteRequest.forDefaultPrivate(user, "my/own/file.txt"))) {
            os.write(("Hello new version").getBytes(StandardCharsets.UTF_8));
        }

        // John takes his mobile phone and application checks if it needs to sync content
        Instant savedOnPC = versionedServices.latestPrivate()
                .list(ListRequest.forDefaultPrivate(user, "my/own/file.txt"))
                .findAny().get().getResource().getModifiedAt();

        // This indicates that we need to update our cache on mobile phone
        // Modified date of saved file has changed and it is newer that our cached date
        // So mobile application should download latest file version
        assertThat(savedOnPC).isAfter(savedOnMobile);
        // END_SNIPPET
    }

    private UserIDAuth registerUser(String username) {
        UserIDAuth creds = new UserIDAuth(username, ReadKeyPasswordTestFactory.getForString("passwrd" + username));
        versionedServices.userProfile().registerUsingDefaults(creds);
        return creds;
    }
}
