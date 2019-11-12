package de.adorsys.datasafe.examples.business.filesystem;

import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CustomlyBuiltDatasafeServiceTest {

    @Test
    void testPathEncryptionOverridden(@TempDir Path root) {
        // BEGIN_SNIPPET:Create custom-built Datasafe service
        // Customized service, we create required module using compile time DI provided by Dagger:
        CustomlyBuiltDatasafeServices datasafeServices = DaggerCustomlyBuiltDatasafeServices.builder()
                .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"::toCharArray))
                .storage(new FileSystemStorageService(root))
                .build();

        // registering user
        UserIDAuth user = new UserIDAuth("user", "password"::toCharArray);
        datasafeServices.userProfile().registerUsingDefaults(user);
        // writing into user privatespace, note that with default implementation `file.txt` would be encrypted
        datasafeServices.privateService().write(WriteRequest.forDefaultPrivate(user, "file.txt"));
        // but we see raw filename here:
        assertThat(walk(root)).asString().contains("file.txt");
        // END_SNIPPET
    }

    // not using lombok
    private List<Path> walk(Path root) {
        try (Stream<Path> result = Files.walk(root)){
            return result.collect(Collectors.toList());
        } catch (IOException ex) {
            throw new IllegalStateException("IOException", ex);
        }
    }
}
