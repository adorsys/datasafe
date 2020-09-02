package de.adorsys.datasafe.simple.adapter.impl;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CleanupDbTest extends WithStorageProvider {

    private SimpleDatasafeService simpleDatasafeService;
    private DFSCredentials dfsCredentials;
    private PathEncryptionConfig pathEncryptionConfig = new PathEncryptionConfig(true);

    private void createSimpleService(WithStorageProvider.StorageDescriptor descriptor) {
        dfsCredentials = InitFromStorageProvider.dfsFromDescriptor(descriptor);
        if (dfsCredentials != null) {
            simpleDatasafeService = new SimpleDatasafeServiceImpl(dfsCredentials, new MutableEncryptionConfig(), pathEncryptionConfig);
        } else {
            simpleDatasafeService = new SimpleDatasafeServiceImpl();
        }
    }

    @ParameterizedTest
    @MethodSource("allDefaultStorages")
    void cleanupDb(WithStorageProvider.StorageDescriptor descriptor) {
        createSimpleService(descriptor);

        String content = "content of document";
        String path = "a/b/c.txt";

        UserIDAuth user1 = new UserIDAuth("uzr", ReadKeyPasswordTestFactory.getForString("user"));
        UserIDAuth user2 = new UserIDAuth("other", ReadKeyPasswordTestFactory.getForString("user"));
        simpleDatasafeService.createUser(user1);
        simpleDatasafeService.createUser(user2);

        simpleDatasafeService.storeDocument(user1,  new DSDocument(new DocumentFQN(path),
                new DocumentContent(content.getBytes())));
        simpleDatasafeService.storeDocument(user2,  new DSDocument(new DocumentFQN(path),
                new DocumentContent(content.getBytes())));

        // (1 keystore + 1 pub key + 1 file) * 2
        try (Stream<AbsoluteLocation<ResolvedResource>> ls =
                     descriptor.getStorageService().get()
                             .list(BasePrivateResource.forAbsolutePrivate(descriptor.getLocation()))
        ) {
            assertThat(ls).hasSize(6);
        }

        simpleDatasafeService.cleanupDb();

        try (Stream<AbsoluteLocation<ResolvedResource>> ls =
                     descriptor.getStorageService().get()
                             .list(BasePrivateResource.forAbsolutePrivate(descriptor.getLocation()))
        ) {
            assertThat(ls).isEmpty();
        }
    }
}
