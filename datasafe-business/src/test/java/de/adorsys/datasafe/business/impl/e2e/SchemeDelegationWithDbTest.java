package de.adorsys.datasafe.business.impl.e2e;

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
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.OutputStream;
import java.net.URI;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SchemeDelegationWithDbTest extends WithStorageProvider {

    private static final Set<String> ALLOWED_TABLES = ImmutableSet.of("users", "private_profiles", "public_profiles");

    private Uri minioPath;
    private StorageService minio;
    private StorageService db;
    private DefaultDatasafeServices datasafeServices;

    @BeforeEach
    void initialize() {
        StorageDescriptor minioDescriptor = minio();
        this.minio = minioDescriptor.getStorageService().get();
        this.minioPath = minioDescriptor.getLocation();
        this.db = new DatabaseStorageService(ALLOWED_TABLES, new DatabaseConnectionRegistry(
                uri -> uri.location().getWrapped().getScheme() + ":" + uri.location().getPath().split("/")[1],
                ImmutableMap.of("jdbc://localhost:9999", new DatabaseCredentials("sa", "sa")))
        );

        StorageService multiDfs = new SchemeDelegatingStorage(
                ImmutableMap.of(
                        "s3", minio,
                        "jdbc", db
                )
        );

        this.datasafeServices = DaggerDefaultDatasafeServices
                .builder()
                .config(new ProfilesOnDbDataOnMinio(minioPath.asURI(), URI.create("jdbc://localhost:9999/h2:mem:test/")))
                .storage(multiDfs)
                .build();
    }

    @Test
    @SneakyThrows
    void testProfileOnDbDataOnFsWorks() {
        UserIDAuth userJohn = new UserIDAuth("john", "doe");

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
        
        // File and keystore/pub keys are on FS
        // File and keystore/pub keys are on minio
        assertThat(minio.list(new AbsoluteLocation<>(BasePrivateResource.forPrivate(minioPath.resolve("")))))
            .extracting(it -> minioPath.relativize(it.location()))
            .extracting(it -> it.asURI().toString())
            .contains("john/private/keystore", "john/public/pubkeys")
            .anyMatch(it -> it.startsWith("john/private/files/"))
            .hasSize(3);
    }

    private Stream<String> listDb(String path) {
        return db.list(BasePrivateResource.forAbsolutePrivate(URI.create(path)))
                .map(it -> it.location().asURI().toString());
    }

    static class ProfilesOnDbDataOnMinio extends DefaultDFSConfig {

        private final Uri profilesPath;

        ProfilesOnDbDataOnMinio(URI fsPath, URI profilesPath) {
            super(fsPath, "PAZZWORT");
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
