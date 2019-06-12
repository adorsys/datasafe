package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentContent;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Security;

@Slf4j
public class SimpleDatasafeAdapterTest {
    SimpleDatasafeService simpleDatasafeService;
    UserIDAuth userIDAuth;

    @BeforeEach
    public void before() {
        Security.addProvider(new BouncyCastleProvider());

        simpleDatasafeService = new SimpleDatasafeServiceImpl();
        userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));
        simpleDatasafeService.createUser(userIDAuth);
    }

    @AfterEach
    public void after() {
        // TODO fixme
        simpleDatasafeService.destroyUser(userIDAuth);
    }

   @Test
    public void justCreateAndDeleteUser() {
        // do nothing;
    }

   //@Test
    // TODO fixme (deregister not working yet)
    public  void writeAndReadFile() {
        String content = "content of document";
        String path = "a/b/c.txt";
        DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);

        DSDocument dsDocument = simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(path));
        Assertions.assertArrayEquals(content.getBytes(), dsDocument.getDocumentContent().getValue());
    }
}
