package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.SchemeDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.db.DatabaseConnectionRegistry;
import de.adorsys.datasafe.storage.impl.db.DatabaseCredentials;
import de.adorsys.datasafe.storage.impl.db.DatabaseStorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.datasafe.types.api.global.PathEncryptionId.AES_SIV;
import static org.assertj.core.api.Assertions.assertThat;

class SchemeDelegationWithDbTest extends WithStorageProvider {

    private static final Set<String> ALLOWED_TABLES = ImmutableSet.of("users", "private_profiles", "public_profiles");

    private Path fsPath;
    private StorageService db;
    private DefaultDatasafeServices datasafeServices;

    @BeforeEach
    void initialize(@TempDir Path tempDir) {
        this.fsPath = tempDir;
        StorageService filesystem = new FileSystemStorageService(tempDir);
        this.db = new DatabaseStorageService(ALLOWED_TABLES, new DatabaseConnectionRegistry(
            uri -> uri.location().getWrapped().getScheme() + ":" + uri.location().getPath().split("/")[1],
            ImmutableMap.of("jdbc://localhost:9999", new DatabaseCredentials("sa", "sa")))
        );

        StorageService multiDfs = new SchemeDelegatingStorage(
            ImmutableMap.of(
                "file", filesystem,
                "jdbc", db
            )
        );

        this.datasafeServices = DaggerDefaultDatasafeServices
            .builder()
            .config(new ProfilesOnDbDataOnFs(tempDir.toUri(), URI.create("jdbc://localhost:9999/h2:mem:test/")))
            .storage(multiDfs)
            .build();
    }

    @Test
    @SneakyThrows
    void testProfileOnDbDataOnFsWorks() {
        UserIDAuth userJohn = new UserIDAuth("john", ReadKeyPasswordTestFactory.getForString("doe"));

        // John's profile will be saved to Database
        datasafeServices.userProfile().registerUsingDefaults(userJohn);

        // But this data - it will be saved to FS
        try (OutputStream os =
                 datasafeServices.privateService().write(WriteRequest.forDefaultPrivate(userJohn, "file.txt"))) {
            os.write("Hello".getBytes());
        }

        // Profiles are on DB
        assertThat(listDb("jdbc://localhost:9999/h2:mem:test/private_profiles/"))
            .containsExactly("jdbc://localhost:9999/h2:mem:test/private_profiles/john");
        assertThat(listDb("jdbc://localhost:9999/h2:mem:test/public_profiles/"))
            .containsExactly("jdbc://localhost:9999/h2:mem:test/public_profiles/john");

        Path path = fsPath.resolve(new Uri("users/john/private/files/").resolve(AES_SIV.asUriRoot()).asString());
        Path encryptedFile = walk(path).get(1);
        // File and keystore/pub keys are on FS
        assertThat(walk(fsPath))
            .extracting(it -> fsPath.toUri().relativize(it.toUri()))
            .extracting(URI::toString)
            .containsExactlyInAnyOrder(
                "",
                "users/",
                "users/john/",
                "users/john/public/",
                "users/john/public/pubkeys",
                "users/john/private/",
                "users/john/private/keystore",
                "users/john/private/files/",
                "users/john/private/files/SIV/",
                fsPath.toUri().relativize(encryptedFile.toUri()).toString()
            );
    }

    @SneakyThrows
    private List<Path> walk(Path at) {
        try (Stream<Path> ls = Files.walk(at)) {
            return ls.collect(Collectors.toList());
        }
    }

    private List<String> listDb(String path) {
        try (Stream<AbsoluteLocation<ResolvedResource>> stream = db.list(BasePrivateResource.forAbsolutePrivate(URI.create(path)))){
            return stream.map(it -> it.location().asURI().toString()).collect(Collectors.toList());
        }
    }

    static class ProfilesOnDbDataOnFs extends DefaultDFSConfig {

        private final Uri profilesPath;

        ProfilesOnDbDataOnFs(URI fsPath, URI profilesPath) {
            super(fsPath, new ReadStorePassword("PAZZWORT"));
            this.profilesPath = new Uri(profilesPath);
        }

        @Override
        public AbsoluteLocation publicProfile(UserID forUser) {
            return new AbsoluteLocation<>(
                BasePrivateResource.forPrivate(
                    profilesPath.resolve("public_profiles/").resolve(forUser.getValue())
                )
            );
        }

        @Override
        public AbsoluteLocation privateProfile(UserID forUser) {
            return new AbsoluteLocation<>(
                BasePrivateResource.forPrivate(
                    profilesPath.resolve("private_profiles/").resolve(forUser.getValue())
                )
            );
        }
    }
}
