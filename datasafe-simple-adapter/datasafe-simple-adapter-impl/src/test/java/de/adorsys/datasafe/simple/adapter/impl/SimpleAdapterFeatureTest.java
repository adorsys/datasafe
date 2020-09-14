package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.simple.adapter.api.types.AmazonS3DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentialsFactory;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentContent;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import de.adorsys.datasafe.simple.adapter.api.types.FilesystemDFSCredentials;
import de.adorsys.datasafe.simple.adapter.impl.cmsencryption.SwitchableCmsEncryptionImpl;
import de.adorsys.datasafe.simple.adapter.impl.config.PathEncryptionConfig;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
class SimpleAdapterFeatureTest extends BaseMockitoTest {
    
    private UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), ReadKeyPasswordTestFactory.getForString("password"));
    private String content = "content of document";
    private String path = "a/b/c.txt";
    private DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));

    @BeforeEach
    @AfterEach
    void afterEach() {
        System.setProperty(SwitchableCmsEncryptionImpl.NO_CMSENCRYPTION_AT_ALL, Boolean.FALSE.toString());

    }

    @Test
    void testWithEncryption() {
        SimpleDatasafeServiceImpl simpleDatasafeService = new SimpleDatasafeServiceImpl();
        simpleDatasafeService.createUser(userIDAuth);
        simpleDatasafeService.storeDocument(userIDAuth, document);

        AbsoluteLocation<PrivateResource> rootLocation = getPrivateResourceAbsoluteLocation();
        try (Stream<AbsoluteLocation<ResolvedResource>> stream = simpleDatasafeService.getStorageService().list(rootLocation)) {
            Assertions.assertEquals(0, stream.filter(el -> el.location().toASCIIString().contains(path)).count());
        }
        simpleDatasafeService.destroyUser(userIDAuth);
    }

    @Test
    @SneakyThrows
    void testWithoutPathEncryption() {
        SimpleDatasafeServiceImpl simpleDatasafeService = new SimpleDatasafeServiceImpl(new PathEncryptionConfig(false));
        simpleDatasafeService.createUser(userIDAuth);
        simpleDatasafeService.storeDocument(userIDAuth, document);

        AbsoluteLocation<PrivateResource> rootLocation = getPrivateResourceAbsoluteLocation();
        try (Stream<AbsoluteLocation<ResolvedResource>> absoluteLocationStream = simpleDatasafeService.getStorageService().list(rootLocation).filter(el -> el.location().toASCIIString().contains(path))) {
            Assertions.assertEquals(1, absoluteLocationStream.count());
        }
        try (Stream<AbsoluteLocation<ResolvedResource>> absoluteLocationStream = simpleDatasafeService.getStorageService().list(rootLocation).filter(el -> el.location().toASCIIString().contains(path))) {
            Optional<AbsoluteLocation<ResolvedResource>> first = absoluteLocationStream.findFirst();

            try (InputStream read = simpleDatasafeService.getStorageService().read(first.get())) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(read, writer, UTF_8);
                assertFalse(writer.toString().equals(content));
            }
        }
        simpleDatasafeService.destroyUser(userIDAuth);
    }


    @Test
    @SneakyThrows
    void testWithoutEncryption() {
        System.setProperty(SwitchableCmsEncryptionImpl.NO_CMSENCRYPTION_AT_ALL, Boolean.TRUE.toString());
        SimpleDatasafeServiceImpl simpleDatasafeService = new SimpleDatasafeServiceImpl(new PathEncryptionConfig(false));
        simpleDatasafeService.createUser(userIDAuth);
        simpleDatasafeService.storeDocument(userIDAuth, document);

        AbsoluteLocation<PrivateResource> rootLocation = getPrivateResourceAbsoluteLocation();
        try (Stream<AbsoluteLocation<ResolvedResource>> absoluteLocationStream = simpleDatasafeService.getStorageService().list(rootLocation).filter(el -> el.location().toASCIIString().contains(path))) {
            Assertions.assertEquals(1, absoluteLocationStream.count());
        }
        try (Stream<AbsoluteLocation<ResolvedResource>> absoluteLocationStream = simpleDatasafeService.getStorageService().list(rootLocation).filter(el -> el.location().toASCIIString().contains(path))) {
            Optional<AbsoluteLocation<ResolvedResource>> first =absoluteLocationStream.findFirst();

            try (InputStream read = simpleDatasafeService.getStorageService().read(first.get())) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(read, writer, UTF_8);
                assertTrue(writer.toString().equals(content));
            }
        }
        simpleDatasafeService.destroyUser(userIDAuth);
    }

    @Nullable
    private AbsoluteLocation<PrivateResource> getPrivateResourceAbsoluteLocation() {
        DFSCredentials credentials = DFSCredentialsFactory.getFromEnvironmnet();
        AbsoluteLocation<PrivateResource> rootLocation = null;
        if (credentials instanceof FilesystemDFSCredentials) {
            String root = ((FilesystemDFSCredentials) credentials).getRoot();
            Path listpath = FileSystems.getDefault().getPath(root);
            rootLocation = new AbsoluteLocation<>(BasePrivateResource.forPrivate(listpath.toUri()));
        }
        if (credentials instanceof AmazonS3DFSCredentials) {
            String root = ((AmazonS3DFSCredentials) credentials).getRootBucket();
        }
        return rootLocation;
    }

}
