package de.adorsys.datasafe.simple.adapter.impl;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.*;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.Streams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.file.NoSuchFileException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                dfsCredentials = FilesystemDFSCredentials.builder().root(descriptor.getRootBucket()).build();
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

    private static Stream<StorageDescriptor> storages() {
            return allDefaultStorages();
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
    @MethodSource("storages")
    @SneakyThrows
    public void justCreateAndDeleteUser(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
        mystart();
        log.info("test create user and delete user with " + descriptor.getName());
    }

    @ParameterizedTest
    @MethodSource("storages")
    public void writeAndReadFile(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
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
    @MethodSource("storages")
    void writeAndReadFileWithPasswordChange(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
        mystart();
        String content = "content of document";
        String path = "a/b/c.txt";
        DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);
        ReadKeyPassword newPassword = new ReadKeyPassword("AAAAAAHHH!");

        simpleDatasafeService.changeKeystorePassword(userIDAuth, newPassword);
        assertThrows(
                UnrecoverableKeyException.class,
                () -> simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(path))
        );

        userIDAuth = new UserIDAuth(userIDAuth.getUserID(), newPassword);
        DSDocument dsDocument = simpleDatasafeService.readDocument(userIDAuth, new DocumentFQN(path));
        assertTrue(simpleDatasafeService.documentExists(userIDAuth, document.getDocumentFQN()));
        assertFalse(simpleDatasafeService.documentExists(userIDAuth, new DocumentFQN("doesnotexist.txt")));

        assertArrayEquals(content.getBytes(), dsDocument.getDocumentContent().getValue());
        log.info("the content read is ok");
    }

    @ParameterizedTest
    @MethodSource("storages")
    public void writeAndReadFileWithSlash(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
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
    @MethodSource("storages")
    public void writeAndReadFiles(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
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

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("storages")
    public void writeAndReadFilesAsStream(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
        mystart();

        DocumentFQN path = new DocumentFQN("file.txt");
        byte[] bytes = "Bytes".getBytes();
        simpleDatasafeService.storeDocumentStream(
                userIDAuth,
                new DSDocumentStream(path, new ByteArrayInputStream(bytes))
        );

        DSDocumentStream ds = simpleDatasafeService.readDocumentStream(userIDAuth, path);
        assertArrayEquals(Streams.readAll(ds.getDocumentStream()), bytes);

        byte[] otherBytes = "otherBytes".getBytes();
        try (OutputStream os = simpleDatasafeService.storeDocumentStream(userIDAuth, path)) {
            os.write(otherBytes);
        }

        DSDocumentStream otherDs = simpleDatasafeService.readDocumentStream(userIDAuth, path);
        assertArrayEquals(Streams.readAll(otherDs.getDocumentStream()), otherBytes);
    }

    @ParameterizedTest
    @MethodSource("storages")
    public void testTwoUsers(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
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
        // TODO: better check
        // users' keystore is dropped from cache, so it is not possible to find decrypted path
        // because access to keystore throws exception
        if (descriptor.getStorageService().get() instanceof FileSystemStorageService) {
            assertThrows(
                NoSuchFileException.class,
                () -> simpleDatasafeService.documentExists(userIDAuth2, document.getDocumentFQN())
            );
        } else {
            assertThrows(
                AmazonS3Exception.class,
                () -> simpleDatasafeService.documentExists(userIDAuth2, document.getDocumentFQN())
            );
        }

        assertFalse(simpleDatasafeService.userExists(userIDAuth2.getUserID()));

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
