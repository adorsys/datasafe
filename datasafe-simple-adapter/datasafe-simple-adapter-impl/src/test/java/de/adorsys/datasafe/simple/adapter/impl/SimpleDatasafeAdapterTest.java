package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.business.impl.e2e.DatasafeServicesProvider;
import de.adorsys.datasafe.business.impl.e2e.WithStorageProvider;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentContent;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import de.adorsys.datasafe.storage.api.StorageService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.Security;

@Slf4j
public class SimpleDatasafeAdapterTest extends WithStorageProvider {
    SimpleDatasafeService simpleDatasafeService;
    UserIDAuth userIDAuth;

    private void init(WithStorageProvider.StorageDescriptor descriptor) {
        descriptor.getName();
        descriptor.getLocation();
        StorageService storageService = descriptor.getStorageService().get();
    }

    @BeforeEach
    public void before() {
        Security.addProvider(new BouncyCastleProvider());

        simpleDatasafeService = new SimpleDatasafeServiceImpl();
        userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));
        simpleDatasafeService.createUser(userIDAuth);
    }

    @AfterEach
    public void after() {
        simpleDatasafeService.destroyUser(userIDAuth);
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    public void justCreateAndDeleteUser(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);
        log.info("test create user and delete user with "  + descriptor.getName());
        // do nothing;
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    public void writeAndReadFile(WithStorageProvider.StorageDescriptor descriptor) {
        log.info("test create user and create files and delete user with " + descriptor.getName());
        String content = "content of document";
        String path = "a/b/c.txt";
        DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);

        DSDocument dsDocument = simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(path));
        Assertions.assertArrayEquals(content.getBytes(), dsDocument.getDocumentContent().getValue());
    }
}
