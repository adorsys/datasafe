package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentContent;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import lombok.SneakyThrows;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;

class DFSRelativeToRootProfileTest extends WithStorageProvider {

    private SimpleDatasafeService simpleDatasafeService;
    private UserIDAuth userIDAuth;

    @BeforeEach
    void mybefore() {
        Security.addProvider(new BouncyCastleProvider());
    }

    void createDatasafeAdapter(StorageDescriptor descriptor) {
        DFSCredentials credentials = InitFromStorageProvider.dfsFromDescriptor(descriptor);

        simpleDatasafeService =
                null != credentials ? new SimpleDatasafeServiceImpl(credentials) : new SimpleDatasafeServiceImpl();

        userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));
        simpleDatasafeService.createUser(userIDAuth);
    }


    @ParameterizedTest
    @MethodSource("allDefaultStorages")
    @SneakyThrows
    void createWriteAndDeleteUserCleansAllNeeded(WithStorageProvider.StorageDescriptor descriptor) {
        createDatasafeAdapter(descriptor);

        DSDocument document = new DSDocument(new DocumentFQN("c.txt"), new DocumentContent("Hello".getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);

        simpleDatasafeService.destroyUser(userIDAuth);

        // Everything is cleaned up:
        assertThat(descriptor.getStorageService().get().list(
                BasePrivateResource.forAbsolutePrivate(descriptor.getLocation()))
        ).isEmpty();
    }
}
