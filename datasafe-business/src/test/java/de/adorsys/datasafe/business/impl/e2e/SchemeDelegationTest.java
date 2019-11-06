package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.SchemeDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
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
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SchemeDelegationTest extends WithStorageProvider {

    private Path fsPath;
    private Uri minioPath;
    private StorageService minio;
    private StorageService filesystem;
    private DefaultDatasafeServices datasafeServices;

    @BeforeEach
    void initialize(@TempDir Path tempDir) {
        WithStorageProvider.StorageDescriptor minioDescriptor = minio();
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
        UserIDAuth userJohn = new UserIDAuth("john", ReadKeyPasswordTestFactory.getForString("doe"));

        // John's profile will be saved to filesystem
        datasafeServices.userProfile().registerUsingDefaults(userJohn);

        // But this data - it will be saved to minio
        try (OutputStream os =
                     datasafeServices.privateService().write(WriteRequest.forDefaultPrivate(userJohn, "file.txt"))) {
            os.write("Hello".getBytes());
        }

        // Profiles are on FS
        assertThat(listFs())
                .extracting(it -> fsPath.relativize(it))
                .extracting(Path::toString)
                .containsExactlyInAnyOrder("", "public-john", "private-john");
        // File and keystore/pub keys are on minio
        assertThat(listMinio())
                .extracting(it -> minioPath.relativize(it.location()))
                .extracting(it -> it.asURI().toString())
                .contains("users/john/private/keystore", "users/john/public/pubkeys")
                .anyMatch(it -> it.startsWith("users/john/private/files/"))
                .hasSize(3);
    }

    private List<AbsoluteLocation<ResolvedResource>> listMinio() {
        try (Stream<AbsoluteLocation<ResolvedResource>> ls =
                     minio.list(new AbsoluteLocation<>(BasePrivateResource.forPrivate(minioPath.resolve(""))))) {
            return ls.collect(Collectors.toList());
        }
    }

    private List<Path> listFs() throws IOException {
        try (Stream<Path> ls = Files.walk(fsPath)) {
            return ls.collect(Collectors.toList());
        }
    }

    static class ProfilesOnFsDataOnMinio extends DefaultDFSConfig {

        private final Path profilesPath;

        ProfilesOnFsDataOnMinio(Uri minioBucketPath, Path profilesPath) {
            super(minioBucketPath, new ReadStorePassword("PAZZWORT"));
            this.profilesPath = profilesPath;
        }

        @Override
        public AbsoluteLocation publicProfile(UserID forUser) {
            return new AbsoluteLocation<>(
                    BasePrivateResource.forPrivate(profilesPath.resolve("public-" + forUser.getValue()).toUri())
            );
        }

        @Override
        public AbsoluteLocation privateProfile(UserID forUser) {
            return new AbsoluteLocation<>(
                    BasePrivateResource.forPrivate(profilesPath.resolve("private-" + forUser.getValue()).toUri())
            );
        }
    }
}
