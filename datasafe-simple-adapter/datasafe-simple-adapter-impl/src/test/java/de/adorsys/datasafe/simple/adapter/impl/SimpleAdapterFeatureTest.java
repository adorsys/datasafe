package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.types.*;
import de.adorsys.datasafe.simple.adapter.impl.cmsencryption.SwitchableCmsEncryptionImpl;
import de.adorsys.datasafe.simple.adapter.impl.pathencryption.SwitchablePathEncryptionImpl;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
class SimpleAdapterFeatureTest extends WithBouncyCastle {
    
    private UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), ReadKeyPassword.getForString("password"));
    private String content = "content of document";
    private String path = "a/b/c.txt";
    private DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));

    @BeforeEach
    @AfterEach
    void afterEach() {
        System.setProperty(SwitchablePathEncryptionImpl.NO_BUCKETPATH_ENCRYPTION, Boolean.FALSE.toString());
        System.setProperty(SwitchableCmsEncryptionImpl.NO_CMSENCRYPTION_AT_ALL, Boolean.FALSE.toString());
    }

    @Test
    void testWithEncryption() {
        SimpleDatasafeServiceImpl simpleDatasafeService = new SimpleDatasafeServiceImpl();
        simpleDatasafeService.createUser(userIDAuth);
        simpleDatasafeService.storeDocument(userIDAuth, document);

        AbsoluteLocation<PrivateResource> rootLocation = getPrivateResourceAbsoluteLocation();
        Assertions.assertEquals(0, simpleDatasafeService.getStorageService().list(rootLocation).filter(el -> el.location().toASCIIString().contains(path)).count());
        simpleDatasafeService.destroyUser(userIDAuth);
    }

    @Test
    @SneakyThrows
    void testWithoutPathEncryption() {
        System.setProperty(SwitchablePathEncryptionImpl.NO_BUCKETPATH_ENCRYPTION, Boolean.TRUE.toString());
        SimpleDatasafeServiceImpl simpleDatasafeService = new SimpleDatasafeServiceImpl();
        simpleDatasafeService.createUser(userIDAuth);
        simpleDatasafeService.storeDocument(userIDAuth, document);

        AbsoluteLocation<PrivateResource> rootLocation = getPrivateResourceAbsoluteLocation();
        Assertions.assertEquals(1, simpleDatasafeService.getStorageService().list(rootLocation).filter(el -> el.location().toASCIIString().contains(path)).count());
        Optional<AbsoluteLocation<ResolvedResource>> first = simpleDatasafeService.getStorageService().list(rootLocation).filter(el -> el.location().toASCIIString().contains(path)).findFirst();
        InputStream read = simpleDatasafeService.getStorageService().read(first.get());
        StringWriter writer = new StringWriter();
        IOUtils.copy(read, writer, StandardCharsets.UTF_8);
        assertFalse(writer.toString().equals(content));
        simpleDatasafeService.destroyUser(userIDAuth);
    }


    @Test
    @SneakyThrows
    void testWithoutEncryption() {
        System.setProperty(SwitchablePathEncryptionImpl.NO_BUCKETPATH_ENCRYPTION, Boolean.TRUE.toString());
        System.setProperty(SwitchableCmsEncryptionImpl.NO_CMSENCRYPTION_AT_ALL, Boolean.TRUE.toString());
        SimpleDatasafeServiceImpl simpleDatasafeService = new SimpleDatasafeServiceImpl();
        simpleDatasafeService.createUser(userIDAuth);
        simpleDatasafeService.storeDocument(userIDAuth, document);

        AbsoluteLocation<PrivateResource> rootLocation = getPrivateResourceAbsoluteLocation();
        Assertions.assertEquals(1, simpleDatasafeService.getStorageService().list(rootLocation).filter(el -> el.location().toASCIIString().contains(path)).count());
        Optional<AbsoluteLocation<ResolvedResource>> first = simpleDatasafeService.getStorageService().list(rootLocation).filter(el -> el.location().toASCIIString().contains(path)).findFirst();
        InputStream read = simpleDatasafeService.getStorageService().read(first.get());
        StringWriter writer = new StringWriter();
        IOUtils.copy(read, writer, StandardCharsets.UTF_8);
        assertTrue(writer.toString().equals(content));
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
