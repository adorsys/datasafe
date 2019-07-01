package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.SchemeDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileWithUtf8Test extends WithStorageProvider {

    private Path fsPath;
    private Uri minioPath;
    private StorageService minio;
    private StorageService filesystem;
    private DefaultDatasafeServices datasafeServices;

    @BeforeEach
    void initialize(@TempDir Path tempDir) {
        StorageDescriptor minioDescriptor = minio();
        this.fsPath = tempDir;
        this.minio = minioDescriptor.getStorageService().get();
        this.filesystem = new FileSystemStorageService(tempDir);
        this.minioPath = minioDescriptor.getLocation();
        StorageService multiDfs = new SchemeDelegatingStorage(
                ImmutableMap.of(
                        "s3", minio,
                        "file", filesystem
                )
        );

        this.datasafeServices = DaggerDefaultDatasafeServices
                .builder()
                .config(new ProfilesOnFsDataOnMinio(minioPath, tempDir))
                .storage(multiDfs)
                .build();
    }

    @Test
    @SneakyThrows
    void testProfileOnFsDataOnMinioWorks() {
        UserIDAuth userJohn = new UserIDAuth("john", "doe");

        // John's profile will be saved to filesystem
        datasafeServices.userProfile().registerUsingDefaults(userJohn);

        // But this data - it will be saved to minio
        try (OutputStream os =
                     datasafeServices.privateService().write(WriteRequest.forDefaultPrivate(userJohn, "file.txt"))) {
            os.write("Hello".getBytes());
        }

        // Profiles are on FS
        assertThat(Files.walk(fsPath))
                .extracting(it -> fsPath.relativize(it))
                .extracting(Path::toString)
                .containsExactlyInAnyOrder("", "public-john", "private-john");
        // File and keystore/pub keys are on minio
        assertThat(minio.list(new AbsoluteLocation<>(BasePrivateResource.forPrivate(minioPath.resolve("")))))
                .extracting(it -> minioPath.relativize(it.location()))
                .extracting(it -> it.asURI().toString())
                .contains("users/john/private/keystore", "users/john/public/pubkeys")
                .anyMatch(it -> it.startsWith("users/john/private/files/"))
                .hasSize(3);
    }

    static class ProfilesOnFsDataOnMinio extends DefaultDFSConfig {

        private final Path profilesPath;

        ProfilesOnFsDataOnMinio(Uri minioBucketPath, Path profilesPath) {
            super(minioBucketPath, "PAZZWORT");
            this.profilesPath = profilesPath;
        }

        @Override
        public AbsoluteLocation publicProfile(UserID forUser) {
            return new AbsoluteLocation<>(
                    BasePrivateResource.forPrivate(profilesPath.resolve(
                            "prüfungs/мой профиль публичный-" + forUser.getValue()).toUri())
            );
        }

        @Override
        public AbsoluteLocation privateProfile(UserID forUser) {
            return new AbsoluteLocation<>(
                    BasePrivateResource.forPrivate(
                            profilesPath.resolve("prüfungs/мой профиль приватный-" + forUser.getValue()).toUri())
            );
        }

        @Override
        protected Uri userRoot(UserID userID) {
            return super.userRoot(userID).resolve("root prüfungs");
        }
    }
}