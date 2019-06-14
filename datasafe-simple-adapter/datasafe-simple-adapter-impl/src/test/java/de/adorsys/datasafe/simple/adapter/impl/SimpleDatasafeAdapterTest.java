package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.business.impl.e2e.WithStorageProvider;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.FileSystems;
import java.security.Security;

@Slf4j
public class SimpleDatasafeAdapterTest extends WithStorageProvider {
    SimpleDatasafeService simpleDatasafeService;
    UserIDAuth userIDAuth;
    DFSCredentials dfsCredentials;

    private void myinit(WithStorageProvider.StorageDescriptor descriptor) {
        if (descriptor == null) {
            dfsCredentials = null;
            return;
        }
        switch (descriptor.getName()) {
            case FILESYSTEM: {
                log.info("uri:" + descriptor.getRootBucket());
                dfsCredentials = FilesystemDFSCredentials.builder().root(FileSystems.getDefault().getPath(descriptor.getRootBucket())).build();
                break;

            }
            case MINIO:
            case CEPH:
            case AMAZON: {
                descriptor.getStorageService().get();
                log.info("uri       :" + descriptor.getLocation());
                log.info("accesskey :" + descriptor.getAccessKey());
                log.info("secretkey :" + descriptor.getSecretKey());
                log.info("region    :" + descriptor.getRegion());
                log.info("rootbucket:" + descriptor.getRootBucket());
                log.info("mapped uri:" + descriptor.getMappedUrl());
                dfsCredentials = AmazonS3DFSCredentials.builder()
                        .accessKey(descriptor.getAccessKey())
                        .secretKey(descriptor.getSecretKey())
                        .region(descriptor.getRegion())
                        .rootBucket(descriptor.getRootBucket())
                        .url(descriptor.getMappedUrl())
                        .build();
                break;
            }
            default:
                throw new SimpleAdapterException("missing switch for " + descriptor.getName());
        }
    }

    @BeforeEach
    public void mybefore() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public void mystart() {
        if (dfsCredentials != null) {
            simpleDatasafeService = new SimpleDatasafeServiceImpl(dfsCredentials);
        } else {
            simpleDatasafeService = new SimpleDatasafeServiceImpl();
        }
        userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));
        simpleDatasafeService.createUser(userIDAuth);
    }

    @AfterEach
    public void myafter() {
        log.info("delete user");
        simpleDatasafeService.destroyUser(userIDAuth);
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    @SneakyThrows
    public void justCreateAndDeleteUser(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
        mystart();
        log.info("test create user and delete user with " + descriptor.getName());
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    public void writeAndReadFile(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
        mystart();
        String content = "content of document";
        String path = "a/b/c.txt";
        DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);

        DSDocument dsDocument = simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(path));
        Assertions.assertArrayEquals(content.getBytes(), dsDocument.getDocumentContent().getValue());
        log.info("the content read is ok");
    }
}
