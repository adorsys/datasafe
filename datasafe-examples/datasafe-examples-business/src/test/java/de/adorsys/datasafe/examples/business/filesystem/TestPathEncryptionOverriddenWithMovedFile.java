package de.adorsys.datasafe.examples.business.filesystem;

import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImplRuntimeDelegatable;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class TestPathEncryptionOverriddenWithMovedFile {

    @Test
    @SneakyThrows
    void testPathEncryptionOverridden(@TempDir Path root) {
        // BEGIN_SNIPPET:Create overridable Datasafe services without recompilation
        // This shows how to override path encryption service, in particular we are going to disable it
        OverridesRegistry registry = new BaseOverridesRegistry();

        // PathEncryptionImpl now will have completely different functionality
        // instead of calling PathEncryptionImpl methods we will call PathEncryptionImplOverridden methods
        PathEncryptionImplRuntimeDelegatable.overrideWith(registry, PathEncryptionImplOverridden::new);

        // registering user
        UserIDAuth user = new UserIDAuth("user", "passwrd"::toCharArray);

        DefaultDatasafeServices defaultDatasafeServices = DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"::toCharArray))
                .storage(new FileSystemStorageService(root))
                .build();

        defaultDatasafeServices.userProfile().registerUsingDefaults(user);
        OutputStream defOs = defaultDatasafeServices.privateService().write(WriteRequest.forDefaultPrivate(user, "file-old.txt"));
        defOs.write("HELLO".getBytes(UTF_8));
        defOs.close();
        Path file = Files.walk(root.resolve("users/user/private/files/SIV/")).filter(it -> !it.toFile().isDirectory()).findFirst().get();

        root.resolve("users/user/private/files/folder/SIV").toFile().mkdirs();
        Files.move(file, root.resolve("users/user/private/files/folder/SIV").resolve(file.getFileName()));
        root.resolve("users/user/private/files/SIV").toFile().delete();

        // Customized service, without creating complete module and building it:
        DefaultDatasafeServices datasafeServices = DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"::toCharArray))
                .storage(new FileSystemStorageService(root))
                .overridesRegistry(registry)
                .build();


        datasafeServices.userProfile().registerUsingDefaults(user);
        // writing into user privatespace, note that with default implementation `file.txt` would be encrypted
        OutputStream os = datasafeServices.privateService().write(WriteRequest.forDefaultPrivate(user, "folder/file.txt"));
        os.write("HELLO".getBytes());
        os.close();
        // we can read file by its path
        assertThat(datasafeServices.privateService().read(ReadRequest.forDefaultPrivate(user, "folder/file.txt"))).hasContent("HELLO");
        assertThat(datasafeServices.privateService().read(ReadRequest.forDefaultPrivate(user, "folder/file-old.txt"))).hasContent("HELLO");
        // we can list file
        assertThat(datasafeServices.privateService().list(ListRequest.forDefaultPrivate(user, "folder/")))
                .extracting(it -> it.getResource().asPrivate().decryptedPath().asString())
                .contains("folder/file.txt", "folder/file-old.txt");
        // but we see raw folder name here:
        assertThat(Files.walk(root)).asString().contains("folder");
        // but filename is encrypted:
        assertThat(Files.walk(root)).asString().doesNotContain("file.txt");
        // END_SNIPPET
    }

    // Path encryption that does not encrypt paths
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
