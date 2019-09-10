package de.adorsys.datasafe.simple.adapter.impl;

import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.types.*;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test ensures that SimpleDatasafeAdapter can use setup and folder structure from version 0.4.3
 */
class SimpleDatasafeAdapter043CompatTest extends BaseMockitoTest {

    private UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));
    private SimpleDatasafeServiceImpl simpleDatasafeService;
    private Path dfsRoot;

    @SneakyThrows
    @BeforeEach
    void extractFixtureAndPrepare(@TempDir Path tempDir) {
        Security.addProvider(new BouncyCastleProvider());
        dfsRoot = tempDir;
        Path resources = Paths.get(Resources.getResource("compat-0.4.3").toURI());

        try (Stream<Path> walk = Files.walk(resources)) {
            walk.forEach(resource -> copyResource(tempDir, resources, resource));
        }

        simpleDatasafeService = new SimpleDatasafeServiceImpl(
                FilesystemDFSCredentials.builder().root(tempDir.toString()).build()
        );
    }

    @SneakyThrows
    private void copyResource(@TempDir Path tempDir, Path resourcesRoot, Path resource) {
        Path relative = resourcesRoot.relativize(resource);
        Path inTemp = tempDir.resolve(relative);
        MoreFiles.createParentDirectories(inTemp);

        if (resource.toFile().isDirectory()) {
            return;
        }

        Files.copy(resource, inTemp);
    }

    @Test
    @SneakyThrows
    void writeNewAndReadFileFromOldVersion() {
        String oldContent = "content of document";
        String newContent = "content of NEW document";
        String newPath = "a/b/c-new.txt";
        String oldPath = "a/b/c.txt";

        // write new document to 'old' folder
        DSDocument document = new DSDocument(new DocumentFQN(newPath), new DocumentContent(newContent.getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);

        // validate new document content
        DSDocument dsDocument = simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(newPath));
        assertThat(dsDocument.getDocumentContent().getValue()).isEqualTo(newContent.getBytes());

        // validate old document content
        dsDocument = simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(oldPath));
        assertThat(dsDocument.getDocumentContent().getValue()).isEqualTo(oldContent.getBytes());

        // validate 'old' folder content
        assertThat(simpleDatasafeService.list(userIDAuth, new DocumentDirectoryFQN(""), ListRecursiveFlag.TRUE))
                .extracting(DocumentFQN::getDatasafePath)
                .containsExactlyInAnyOrder(newPath, oldPath);

        // validate folder structure
        assertThat(walkDirNonRecursive(dfsRoot, 1)).containsExactlyInAnyOrder("profiles", "users");
        assertThat(walkDirNonRecursive(dfsRoot.resolve("profiles")))
                .containsExactlyInAnyOrder("private", "public", "private/peter", "public/peter");
        assertThat(walkDirNonRecursive(dfsRoot.resolve("users"), 3))
                .containsExactlyInAnyOrder(
                        "peter",
                        "peter/private",
                        "peter/public",
                        "peter/private/keystore",
                        "peter/private/files",
                        "peter/public/pubkeys"
                );
    }

    @SneakyThrows
    private List<String> walkDirNonRecursive(Path root) {
        return walkDirNonRecursive(root, Integer.MAX_VALUE);
    }

    @SneakyThrows
    private List<String> walkDirNonRecursive(Path root, int depth) {
        try (Stream<Path> walk = Files.walk(root, depth)) {
            return walk
                    .filter(it -> !(it.getFileName().startsWith(".") || it.getFileName().startsWith("..")))
                    .filter(it -> !it.equals(root))
                    .map(it -> root.relativize(it).toString().replaceFirst("\\./", ""))
                    .collect(Collectors.toList());
        }
    }
}
