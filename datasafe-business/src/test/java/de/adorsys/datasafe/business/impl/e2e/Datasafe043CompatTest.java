package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.shared.Dirs;
import de.adorsys.datasafe.types.api.shared.Resources;
import lombok.SneakyThrows;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test ensures that Datasafe can use setup and folder structure from version 0.4.3 (backward compatibility)
 */
class Datasafe043CompatTest extends BaseMockitoTest {

    private static final String BASE_FIXTURE = "compat-0.4.3";
    private static final String OLD_ROOT =
            "file:/var/folders/3w/fzdgs28j1_b3r4y8x9_s4wpw0000gn/T/junit4892767241345613098/";

    private UserIDAuth john = new UserIDAuth(new UserID("john"), new ReadKeyPassword("secure-password john"));
    private UserIDAuth jane = new UserIDAuth(new UserID("jane"), new ReadKeyPassword("secure-password jane"));

    private DefaultDatasafeServices datasafe;
    private Path dfsRoot;

    @SneakyThrows
    @BeforeEach
    void extractFixtureAndPrepare(@TempDir Path tempDir) {
        Security.addProvider(new BouncyCastleProvider());
        dfsRoot = tempDir;

        Resources.copyResourceDir(BASE_FIXTURE, tempDir);
        // Replace original root with new root:
        replace(tempDir.resolve("profiles/private/jane"), OLD_ROOT, tempDir.toUri().toString());
        replace(tempDir.resolve("profiles/private/john"), OLD_ROOT, tempDir.toUri().toString());
        replace(tempDir.resolve("profiles/public/jane"), OLD_ROOT, tempDir.toUri().toString());
        replace(tempDir.resolve("profiles/public/john"), OLD_ROOT, tempDir.toUri().toString());

        datasafe = DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(tempDir.toUri(), "PAZZWORD"))
                .storage(new FileSystemStorageService(tempDir))
                .build();
    }

    @Test
    @SneakyThrows
    void writeNewAndReadFileFromOldVersion() {
        String oldContent = "Hello here 1";
        String newContent = "NEW Hello here 1";
        String newPrivatePath = "folder1/secret-NEW.txt";
        String oldPrivatePath = "folder1/secret.txt";
        String newInboxPath = "hello-NEW.txt";
        String oldInboxPath = "hello.txt";

        // write new document to 'old' folder
        try (OutputStream os = datasafe.privateService().write(WriteRequest.forDefaultPrivate(jane, newPrivatePath))) {
            os.write(newContent.getBytes());
        }

        // read file from private and share this file with John
        try (InputStream is = datasafe.privateService().read(ReadRequest.forDefaultPrivate(jane, newPrivatePath));
             OutputStream os = datasafe.inboxService().write(WriteRequest.forDefaultPublic(
                     Collections.singleton(john.getUserID()),
                     newInboxPath))
        ) {
            ByteStreams.copy(is, os);
        }

        // validate old Jane's private file
        assertThat(datasafe.privateService().read(ReadRequest.forDefaultPrivate(jane, oldPrivatePath)))
                .hasContent(oldContent);
        // validate new Jane's private file
        assertThat(datasafe.privateService().read(ReadRequest.forDefaultPrivate(jane, newPrivatePath)))
                .hasContent(newContent);

        // validate old Johns's inbox file
        assertThat(datasafe.inboxService().read(ReadRequest.forDefaultPrivate(john, oldInboxPath)))
                .hasContent(oldContent);
        // validate new Johns's inbox file
        assertThat(datasafe.inboxService().read(ReadRequest.forDefaultPrivate(john, newInboxPath)))
                .hasContent(newContent);

        // validate folder structure
        assertThat(Dirs.walk(dfsRoot, 1)).containsExactlyInAnyOrder("profiles", "users");
        assertThat(Dirs.walk(dfsRoot.resolve("profiles")))
                .containsExactlyInAnyOrder(
                        "public",
                        "private",
                        "public/john",
                        "public/jane",
                        "private/john",
                        "private/jane"
                );
        assertThat(Dirs.walk(dfsRoot.resolve("users"), 3))
                .containsExactlyInAnyOrder(
                        "john",
                        "john/public",
                        "john/public/inbox",
                        "john/public/pubkeys",
                        "john/private",
                        "john/private/keystore",
                        "jane",
                        "jane/public",
                        "jane/public/pubkeys",
                        "jane/private",
                        "jane/private/keystore",
                        "jane/private/files"
                );
    }

    @SneakyThrows
    private void replace(Path file, String what, String with) {
        List<String> baseContent = Files.readAllLines(file);
        Files.write(file, baseContent.stream().map(it -> it.replace(what, with)).collect(Collectors.toList()));
    }
}
