package de.adorsys.datasafe.simple.adapter.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.FileSystems;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class SimpleDatasafeAdapterTest extends WithStorageProvider {
    SimpleDatasafeService simpleDatasafeService;
    UserIDAuth userIDAuth;
    DFSCredentials dfsCredentials;

    private void myinit(WithStorageProvider.StorageDescriptor descriptor, Boolean encryption) {
        if (descriptor == null) {
            dfsCredentials = null;
            return;
        }
        System.setProperty(SwitchablePathEncryptionImpl.NO_BUCKETPATH_ENCRYPTION, encryption ? Boolean.FALSE.toString(): Boolean.TRUE.toString());

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

    @ValueSource
    protected static Stream<Boolean> withOrWithoutEncryption() {
        return Stream.of(
                Boolean.TRUE,
                Boolean.FALSE);
    }

    private static Stream<Arguments> parameterCombination() {
            return Sets.cartesianProduct(
                    allStorages().collect(Collectors.toSet()),
                    withOrWithoutEncryption().collect(Collectors.toSet())
            ).stream().map(it -> Arguments.of(it.get(0), it.get(1)));
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
    @MethodSource("parameterCombination")
    @SneakyThrows
    public void justCreateAndDeleteUser(WithStorageProvider.StorageDescriptor descriptor, Boolean encryption) {
        myinit(descriptor, encryption);
        mystart();
        log.info("test create user and delete user with " + descriptor.getName());
    }

    @ParameterizedTest
    @MethodSource("parameterCombination")
    public void writeAndReadFile(WithStorageProvider.StorageDescriptor descriptor,  Boolean encryption) {
        myinit(descriptor, encryption);
        mystart();
        String content = "content of document";
        String path = "a/b/c.txt";
        DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);

        DSDocument dsDocument = simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(path));
        assertTrue(simpleDatasafeService.documentExists(userIDAuth, document.getDocumentFQN()));
        assertFalse(simpleDatasafeService.documentExists(userIDAuth, new DocumentFQN("doesnotexist.txt")));

        assertArrayEquals(content.getBytes(), dsDocument.getDocumentContent().getValue());
        log.info("the content read is ok");
    }

    @ParameterizedTest
    @MethodSource("parameterCombination")
    public void writeAndReadFileWithSlash(WithStorageProvider.StorageDescriptor descriptor,  Boolean encryption) {
        myinit(descriptor, encryption);
        mystart();
        String content = "content of document";
        String path = "/a/b/c.txt";
        DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);

        DSDocument dsDocument = simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(path));
        assertTrue(simpleDatasafeService.documentExists(userIDAuth, document.getDocumentFQN()));
        assertFalse(simpleDatasafeService.documentExists(userIDAuth, new DocumentFQN("doesnotexist.txt")));

        assertArrayEquals(content.getBytes(), dsDocument.getDocumentContent().getValue());
        log.info("the content read is ok");
    }


    @ParameterizedTest
    @MethodSource("parameterCombination")
    public void writeAndReadFiles(WithStorageProvider.StorageDescriptor descriptor,  Boolean encryption) {
        myinit(descriptor, encryption);
        mystart();
        DocumentDirectoryFQN root = new DocumentDirectoryFQN("affe");
        List<DSDocument> list = TestHelper.createDocuments(root, 2, 2, 3);
        List<DocumentFQN> created = new ArrayList<>();
        for (DSDocument dsDocument : list) {
            log.debug("store " + dsDocument.getDocumentFQN().toString());
            simpleDatasafeService.storeDocument(userIDAuth, dsDocument);
            created.add(dsDocument.getDocumentFQN());
            assertTrue(simpleDatasafeService.documentExists(userIDAuth, dsDocument.getDocumentFQN()));
        }
        List<DocumentFQN> listFound = simpleDatasafeService.list(userIDAuth, root, ListRecursiveFlag.TRUE);
        show("full list recursive ", listFound);
        assertTrue(created.containsAll(listFound));
        assertTrue(listFound.containsAll(created));

        listFound = simpleDatasafeService.list(userIDAuth, root.addDirectory("subdir_0").addDirectory("subdir_0"), ListRecursiveFlag.TRUE);
        show("subdir 0 subdir 0 recursive", listFound);
        assertEquals(6, listFound.size());

        listFound = simpleDatasafeService.list(userIDAuth, root.addDirectory("subdir_0").addDirectory("subdir_0"), ListRecursiveFlag.FALSE);
        show("subidr 0 subdir 0 non recursive", listFound);
        assertEquals(2, listFound.size());

        listFound = simpleDatasafeService.list(userIDAuth, root.addDirectory("subdir_0").addDirectory("//subdir_0//"), ListRecursiveFlag.FALSE);
        show("subidr 0 subdir 0 non recursive with more slases", listFound);
        assertEquals(2, listFound.size());

        DocumentFQN oneDoc = new DocumentFQN("affe/subdir_0/subdir_0/file1txt");
        assertTrue(simpleDatasafeService.documentExists(userIDAuth, oneDoc));
        simpleDatasafeService.deleteDocument(userIDAuth, oneDoc);
        assertFalse(simpleDatasafeService.documentExists(userIDAuth, oneDoc));

        simpleDatasafeService.deleteFolder(userIDAuth, root.addDirectory("subdir_1"));
        listFound = simpleDatasafeService.list(userIDAuth, root, ListRecursiveFlag.TRUE);
        show("full list recursive after delete subdir 1", listFound);
        assertEquals(15, listFound.size());
        DocumentFQN otherDoc = new DocumentFQN("affe/subdir_0/subdir_0/file0txt");
        simpleDatasafeService.deleteDocument(userIDAuth, otherDoc);
        listFound = simpleDatasafeService.list(userIDAuth, root, ListRecursiveFlag.TRUE);
        show("full list recursive after delete one file", listFound);
        assertEquals(14, listFound.size());
    }

    @ParameterizedTest
    @MethodSource("parameterCombination")
    public void testTwoUsers(WithStorageProvider.StorageDescriptor descriptor,  Boolean encryption) {
        myinit(descriptor, encryption);
        mystart();
        UserIDAuth userIDAuth2 = new UserIDAuth(new UserID("peter2"), new ReadKeyPassword("password2"));
        simpleDatasafeService.createUser(userIDAuth2);

        String content = "content of document";
        String path = "a/b/c.txt";
        DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);

        simpleDatasafeService.storeDocument(userIDAuth2, document);

        // tiny checks, that the password is important
        UserIDAuth wrongPasswordUser1 = new UserIDAuth(userIDAuth.getUserID(),new ReadKeyPassword(UUID.randomUUID().toString()));
        assertThrows(UnrecoverableKeyException.class, () -> simpleDatasafeService.readDocument(wrongPasswordUser1, new DocumentFQN(path)));

        UserIDAuth wrongPasswordUser2 = new UserIDAuth(userIDAuth2.getUserID(),new ReadKeyPassword(UUID.randomUUID().toString()));
        assertThrows(UnrecoverableKeyException.class, () -> simpleDatasafeService.readDocument(wrongPasswordUser2, new DocumentFQN(path)));

        // now read the docs with the correct password
        DSDocument dsDocument = simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(path));
        assertArrayEquals(content.getBytes(), dsDocument.getDocumentContent().getValue());

        DSDocument dsDocument2 = simpleDatasafeService.readDocument(userIDAuth2, new DocumentFQN(path));
        assertArrayEquals(content.getBytes(), dsDocument2.getDocumentContent().getValue());

        simpleDatasafeService.destroyUser(userIDAuth2);
        assertFalse(simpleDatasafeService.documentExists(userIDAuth2, document.getDocumentFQN()));
        assertTrue(simpleDatasafeService.documentExists(userIDAuth, document.getDocumentFQN()));

    }

    private void show(String message, List<DocumentFQN> listFound) {
        log.debug("---------------------------------");
        log.debug(message);
        for (DocumentFQN doc : listFound) {
            log.debug("found:" + doc);
        }
    }

}
