package de.adorsys.datasafe.examples.business.filesystem;

import de.adorsys.datasafe.business.impl.service.DaggerVersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImplRuntimeDelegatable;
import de.adorsys.datasafe.metainfo.version.impl.version.types.DFSVersion;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.resource.Versioned;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * This test shows simplistic usage of Datasafe versioned services that reside on filesystem.
 */
class BaseUserOperationsTestWithUencryptedPath {

    private VersionedDatasafeServices versionedServices;

    /**
     * This shows how you build Software-versioned Datasafe services. Note that you can override any class/module you
     * want by providing your own interface using {@link VersionedDatasafeServices} as a template.
     */
    @BeforeEach
    void createServices() {
        Path root = Paths.get("/home/victoire/temp/tests/");

        OverridesRegistry registry = new BaseOverridesRegistry();

        // Override path encryption to partially encrypt the path
        PathEncryptionImplRuntimeDelegatable.overrideWith(registry, PathEncryptionImplOverridden::new);

        versionedServices = DaggerVersionedDatasafeServices.builder()
                .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"::toCharArray))
                .storage(new FileSystemStorageService(root))
                .overridesRegistry(registry)
                .build();
    }



    /**
     * Writing file - you can write it to versioned private space multiple times and you will see only latest
     */
    @Test
    @SneakyThrows
    void writeFileToVersionedPrivateSpace() {
        UserIDAuth user = registerUser("john");

        // Writing "Hello i" to a versioned file multiple times
        for (int i = 1; i <= 3; ++i) {
            try (OutputStream os = versionedServices.latestPrivate()
                    .write(WriteRequest.forDefaultPrivate(user, "root/my/own/file.txt"))) {
                os.write(("Hello " + i).getBytes(StandardCharsets.UTF_8));
                Thread.sleep(1000L); // Change file modified dates
            }
        }
    }


    @Test
    @SneakyThrows
    void readFileFromVersionedPrivateSpace() {
        UserIDAuth user = new UserIDAuth("john", ReadKeyPasswordTestFactory.getForString("passwrd" + "john"));
        // and still we read only latest file
        assertThat(versionedServices.latestPrivate()
                .read(ReadRequest.forDefaultPrivate(user, "my/own/file.txt"))
        ).hasContent("Hello 3");
        // but there are 3 versions of file stored physically in users' privatespace:
        assertThat(versionedServices.privateService().list(
                ListRequest.forDefaultPrivate(user, "my/own/file.txt"))
        ).hasSize(3);

        // and we know 3 versions of the file
        assertThat(versionedServices.versionInfo().versionsOf(
                ListRequest.forDefaultPrivate(user, "my/own/file.txt"))
        ).hasSize(3);

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


    private UserIDAuth registerUser(String username) {
        UserIDAuth creds = new UserIDAuth(username, ReadKeyPasswordTestFactory.getForString("passwrd" + username));
        versionedServices.userProfile().registerUsingDefaults(creds);
        return creds;
    }

    class PathEncryptionImplOverridden extends PathEncryptionImpl {

        PathEncryptionImplOverridden(PathEncryptionImplRuntimeDelegatable.ArgumentsCaptor captor) {
            super(captor.getSymmetricPathEncryptionService(), captor.getPrivateKeyService());
        }

        @Override
        public Uri encrypt(UserIDAuth forUser, Uri path) {
            if (path.asString().contains("/")) {
                String[] rootAndInRoot = path.asString().split("/", 2);
                return new Uri(URI.create(rootAndInRoot[0] + "/" + super.encrypt(forUser, new Uri(rootAndInRoot[1])).asString()));
            }
            // encryption disabled for root folder:
            return path;
        }

        @Override
        public Function<Uri, Uri> decryptor(UserIDAuth forUser) {
            return rootWithEncrypted -> {
                if (rootWithEncrypted.asString().contains("/")) {
                    String[] rootAndInRoot = rootWithEncrypted.asString().split("/", 2);
                    return new Uri(rootAndInRoot[0] + "/" + super.decryptor(forUser).apply(new Uri(URI.create(rootAndInRoot[1]))).asString());
                }
                // encryption disabled for root folder:
                return rootWithEncrypted;
            };
        }
    }
}