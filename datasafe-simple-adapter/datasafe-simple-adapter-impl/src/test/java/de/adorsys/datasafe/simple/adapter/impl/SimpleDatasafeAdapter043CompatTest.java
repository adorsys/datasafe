package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.types.*;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test ensures that SimpleDatasafeAdapter can use setup and folder structure from version 0.4.3
 */
class SimpleDatasafeAdapter043CompatTest extends BaseMockitoTest {

    private UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));
    private SimpleDatasafeServiceImpl simpleDatasafeService = new SimpleDatasafeServiceImpl(
            FilesystemDFSCredentials.builder().root("compat-0.4.3").build());

    @BeforeEach
    void mybefore() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void writeNewAndReadFileFromOldVersion() {
        String content = "content of document";
        String newPath = "a/b/c-new.txt";
        String oldPath = "a/b/c.txt";

        // write new document to same folder
        DSDocument document = new DSDocument(new DocumentFQN(newPath), new DocumentContent(content.getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);

        // validate new document
        DSDocument dsDocument = simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(newPath));
        assertThat(dsDocument.getDocumentContent().getValue()).asString().isEqualTo(content);

        // validate old document
        dsDocument = simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(oldPath));
        assertThat(dsDocument.getDocumentContent().getValue()).asString().isEqualTo(content);

        assertThat(simpleDatasafeService.list(userIDAuth, new DocumentDirectoryFQN(""), ListRecursiveFlag.TRUE))
                .extracting(DocumentFQN::getDatasafePath)
                .containsExactlyInAnyOrder(newPath, oldPath);

        //cleanup
        simpleDatasafeService.deleteDocument(userIDAuth, new DocumentFQN(newPath));
    }
}
