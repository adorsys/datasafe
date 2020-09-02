package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentContent;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import de.adorsys.datasafe.simple.adapter.impl.config.PathEncryptionConfig;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DFSRelativeToRootProfileTest extends WithStorageProvider {

    private SimpleDatasafeService simpleDatasafeService;
    private UserIDAuth userIDAuth;
    private PathEncryptionConfig pathEncryptionConfig = new PathEncryptionConfig(true);

    void createDatasafeAdapter(StorageDescriptor descriptor) {
        DFSCredentials credentials = InitFromStorageProvider.dfsFromDescriptor(descriptor);

        simpleDatasafeService =
                null != credentials ?
                        new SimpleDatasafeServiceImpl(credentials, new MutableEncryptionConfig(), pathEncryptionConfig)
                        : new SimpleDatasafeServiceImpl();

        userIDAuth = new UserIDAuth(new UserID("peter"), ReadKeyPasswordTestFactory.getForString("password"));
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
        try (Stream<AbsoluteLocation<ResolvedResource>> ls =
                     descriptor.getStorageService().get()
                             .list(BasePrivateResource.forAbsolutePrivate(descriptor.getLocation()))
        ) {
            assertThat(ls).isEmpty();
        }
    }
}
