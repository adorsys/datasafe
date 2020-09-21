package de.adorsys.datasafe.simple.adapter.impl;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.sun.management.UnixOperatingSystemMXBean;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.types.AmazonS3DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocumentStream;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentContent;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentDirectoryFQN;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import de.adorsys.datasafe.simple.adapter.api.types.ListRecursiveFlag;
import de.adorsys.datasafe.simple.adapter.impl.config.PathEncryptionConfig;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.bouncycastle.util.io.Streams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.NoSuchFileException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SimpleDatasafeAdapterTest extends WithStorageProvider {

    private SimpleDatasafeService simpleDatasafeService;
    private UserIDAuth userIDAuth;
    private DFSCredentials dfsCredentials;

    void myinit(StorageDescriptor descriptor) {
        dfsCredentials = InitFromStorageProvider.dfsFromDescriptor(descriptor);
    }

    private static Stream<StorageDescriptor> storages() {
        return allDefaultStorages();
    }

    void mystart() {
        if (dfsCredentials != null) {
            simpleDatasafeService = new SimpleDatasafeServiceImpl(dfsCredentials, new MutableEncryptionConfig(), new PathEncryptionConfig(true));
        } else {
            simpleDatasafeService = new SimpleDatasafeServiceImpl();
        }
        userIDAuth = new UserIDAuth(new UserID("peter"), ReadKeyPasswordTestFactory.getForString("password"));
        simpleDatasafeService.createUser(userIDAuth);
    }

    @AfterEach
    void myafter() {
        log.info("delete user");
        simpleDatasafeService.destroyUser(userIDAuth);
    }

    @ParameterizedTest
    @MethodSource("minioOnly")
    @SneakyThrows
    void justCreateAndDeleteUserForMinioOnly(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
        mystart();

        // SimpleDatasafeAdapter does not use user profile json files, so only keystore and pubkeys should exist:
        try (Stream<AbsoluteLocation<ResolvedResource>> ls = descriptor.getStorageService().get()
                .list(BasePrivateResource.forAbsolutePrivate(descriptor.getLocation()))
        ) {
            assertThat(ls).extracting(it -> descriptor.getLocation().relativize(it.location()).asString())
                    .containsExactlyInAnyOrder(
                            "users/peter/public/pubkeys",
                            "users/peter/private/keystore"
                    );
        }
        log.info("test create user and delete user with  {}", descriptor.getName());
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    @SneakyThrows
    void justCreateAndDeleteUser(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
        mystart();

        // SimpleDatasafeAdapter does not use user profile json files, so only keystore and pubkeys should exist:
        try (Stream<AbsoluteLocation<ResolvedResource>> ls = descriptor.getStorageService().get()
                .list(BasePrivateResource.forAbsolutePrivate(descriptor.getLocation()))
        ) {
            assertThat(ls).extracting(it -> descriptor.getLocation().relativize(it.location()).asString())
                    .containsExactlyInAnyOrder(
                            "users/peter/public/pubkeys",
                            "users/peter/private/keystore"
                    );
        }
        log.info("test create user and delete user with {}", descriptor.getName());
    }

    @ParameterizedTest
    @MethodSource("storages")
    void writeAndReadFile(WithStorageProvider.StorageDescriptor descriptor) {
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
        ReadKeyPassword newPassword = ReadKeyPasswordTestFactory.getForString("AAAAAAHHH!");

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
    void writeAndReadFileWithSlash(WithStorageProvider.StorageDescriptor descriptor) {
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
    void writeAndReadFiles(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
        mystart();
        DocumentDirectoryFQN root = new DocumentDirectoryFQN("affe");
        List<DSDocument> list = TestHelper.createDocuments(root, 2, 2, 3);
        List<DocumentFQN> created = new ArrayList<>();
        for (DSDocument dsDocument : list) {
            log.debug("store {}", dsDocument.getDocumentFQN());
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
    void writeAndReadFilesAsStream(WithStorageProvider.StorageDescriptor descriptor) {
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
    void testTwoUsers(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);
        mystart();
        UserIDAuth userIDAuth2 = new UserIDAuth(new UserID("peter2"), ReadKeyPasswordTestFactory.getForString("password2"));
        simpleDatasafeService.createUser(userIDAuth2);

        String content = "content of document";
        String path = "a/b/c.txt";
        DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);

        simpleDatasafeService.storeDocument(userIDAuth2, document);

        // tiny checks, that the password is important
        UserIDAuth wrongPasswordUser1 = new UserIDAuth(userIDAuth.getUserID(), ReadKeyPasswordTestFactory.getForString(UUID.randomUUID().toString()));
        assertThrows(UnrecoverableKeyException.class, () -> simpleDatasafeService.readDocument(wrongPasswordUser1, new DocumentFQN(path)));

        UserIDAuth wrongPasswordUser2 = new UserIDAuth(userIDAuth2.getUserID(), ReadKeyPasswordTestFactory.getForString(UUID.randomUUID().toString()));
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

    @ParameterizedTest
    @MethodSource("minioOnly")
    @SneakyThrows
    void testExhaustedInputStream(WithStorageProvider.StorageDescriptor descriptor) {
        myinit(descriptor);

        if (!(dfsCredentials instanceof AmazonS3DFSCredentials)) {
            throw new RuntimeException("programming error");
        }
        int maxConnections = 2;
        dfsCredentials = ((AmazonS3DFSCredentials) dfsCredentials).toBuilder()
                .maxConnections(maxConnections)
                .requestTimeout(1000)
                .build();
        mystart();

        String content = "content of document qdm;mwm;ewmfmwemf;we;mfw;emf;llllle";
        String path = "a/b/c.txt";
        DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));
        simpleDatasafeService.storeDocument(userIDAuth, document);

        // create more `not closed requests` than pool can handle
        IntStream.range(0, maxConnections + 1).forEach(it -> {
            DSDocumentStream ds = simpleDatasafeService.readDocumentStream(userIDAuth, document.getDocumentFQN());
            try {
                // This causes exceptions when using authenticated encryption
                ds.getDocumentStream().close();
            } catch (Exception ex) {
                // Ignoring exception, i'm badly behaved client...
            }
        });
    }

    private void show(String message, List<DocumentFQN> listFound) {
        log.debug("---------------------------------");
        log.debug(message);
        for (DocumentFQN doc : listFound) {
            log.debug("found: {}", doc);
        }
    }

}
