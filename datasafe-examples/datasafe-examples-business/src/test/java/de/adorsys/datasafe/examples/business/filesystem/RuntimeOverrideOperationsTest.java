package de.adorsys.datasafe.examples.business.filesystem;

import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImplRuntimeDelegatable;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class RuntimeOverrideOperationsTest {

    @Test
    @SneakyThrows
    void testPathEncryptionOverridden(@TempDir Path root) {
        // BEGIN_SNIPPET:Create overridable Datasafe services without recompilation
        // This shows how to override path encryption service, in particular we are going to disable it
        OverridesRegistry registry = new BaseOverridesRegistry();

        // PathEncryptionImpl now will have completely different functionality
        // instead of calling PathEncryptionImpl methods we will call PathEncryptionImplOverridden methods
        PathEncryptionImplRuntimeDelegatable.overrideWith(registry, PathEncryptionImplOverridden::new);

        // Customized service, without creating complete module and building it:
        DefaultDatasafeServices datasafeServices = DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"::toCharArray))
                .storage(new FileSystemStorageService(root))
                .overridesRegistry(registry)
                .build();

        // registering user
        UserIDAuth user = new UserIDAuth("user", "passwrd"::toCharArray);
        datasafeServices.userProfile().registerUsingDefaults(user);
        // writing into user privatespace, note that with default implementation `file.txt` would be encrypted
        datasafeServices.privateService().write(WriteRequest.forDefaultPrivate(user, "file.txt"));
        // but we see raw filename here:
        assertThat(Files.walk(root)).asString().contains("file.txt");
        // END_SNIPPET
    }

    // Path encryption that does not encrypt paths
    class PathEncryptionImplOverridden extends PathEncryptionImpl {

        PathEncryptionImplOverridden(PathEncryptionImplRuntimeDelegatable.ArgumentsCaptor captor) {
            super(captor.getSymmetricPathEncryptionService(), captor.getPrivateKeyService());
        }

        @Override
        public Uri encrypt(UserIDAuth forUser, Uri path) {
            // encryption disabled:
            return path;
        }

        @Override
        public Function<Uri, Uri> decryptor(UserIDAuth forUser) {
            // encryption disabled:
            return Function.identity();
        }
    }
}
