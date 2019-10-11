package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.simple.adapter.api.types.*;
import de.adorsys.datasafe.types.api.shared.Dirs;
import de.adorsys.datasafe.types.api.shared.Resources;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test ensures that SimpleDatasafeAdapter can use setup and folder structure from version 0.4.3
 * (backward compatibility)
 */
class SimpleDatasafeAdapter043CompatTest extends WithBouncyCastle {

    private UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), ReadKeyPasswordTestFactory.getForString("password"));
    private SimpleDatasafeServiceImpl simpleDatasafeService;
    private Path dfsRoot;

    @SneakyThrows
    @BeforeEach
    void extractFixtureAndPrepare(@TempDir Path tempDir) {
        dfsRoot = tempDir;
        Resources.copyResourceDir("compat-0.4.3", tempDir);
        simpleDatasafeService = new SimpleDatasafeServiceImpl(
                FilesystemDFSCredentials.builder().root(tempDir.toString()).build()
        );
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
        assertThat(Dirs.walk(dfsRoot, 1)).containsExactlyInAnyOrder("profiles", "users");
        assertThat(Dirs.walk(dfsRoot.resolve("profiles")))
                .containsExactlyInAnyOrder("private", "public", "private/peter", "public/peter");
        assertThat(Dirs.walk(dfsRoot.resolve("users"), 3))
                .containsExactlyInAnyOrder(
                        "peter",
                        "peter/private",
                        "peter/public",
                        "peter/private/keystore",
                        "peter/private/files",
                        "peter/public/pubkeys"
                );
    }
}
