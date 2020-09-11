package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.types.AmazonS3DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentContent;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import de.adorsys.datasafe.simple.adapter.api.types.FilesystemDFSCredentials;
import de.adorsys.datasafe.simple.adapter.impl.SimpleDatasafeServiceImpl;
import de.adorsys.datasafe.simple.adapter.spring.annotations.UseDatasafeSpringConfiguration;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@SpringBootConfiguration
@UseDatasafeSpringConfiguration
public class InjectionTest extends WithStorageProvider {

    public void testCreateUser(SimpleDatasafeService datasafeService) {
        assertThat(datasafeService).isNotNull();
        UserID userid = new UserID("peter");
        ReadKeyPassword password = ReadKeyPasswordTestFactory.getForString("password");
        UserIDAuth userIDAuth = new UserIDAuth(userid, password);
        assertThat(datasafeService.userExists(userid)).isFalse();
        datasafeService.createUser(userIDAuth);
        assertThat(datasafeService.userExists(userid)).isTrue();
        datasafeService.destroyUser(userIDAuth);
    }

    @SneakyThrows
    void testWithoutPathEncryption(SimpleDatasafeService simpleDatasafeServiceApi, DFSCredentials dfsCredentials) {
        if (!(simpleDatasafeServiceApi instanceof SimpleDatasafeServiceImpl)) {
            throw new TestException("Did expect instance of SimpleDatasafeServiceImpl");
        }
        AbsoluteLocation<PrivateResource> rootLocation = getPrivateResourceAbsoluteLocation(dfsCredentials);
        SimpleDatasafeServiceImpl simpleDatasafeService = (SimpleDatasafeServiceImpl) simpleDatasafeServiceApi;

        UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), ReadKeyPasswordTestFactory.getForString("password"));
        String content = "content of document";
        String path = "a/b/c.txt";
        DSDocument document = new DSDocument(new DocumentFQN(path), new DocumentContent(content.getBytes()));
        simpleDatasafeService.createUser(userIDAuth);
        simpleDatasafeService.storeDocument(userIDAuth, document);

        try (Stream<AbsoluteLocation<ResolvedResource>> absoluteLocationStream = simpleDatasafeService.getStorageService().list(rootLocation)) {
            assertEquals(1, absoluteLocationStream.filter(el -> el.location().toASCIIString().contains(path)).count());
        }
        try (Stream<AbsoluteLocation<ResolvedResource>> absoluteLocationStream = simpleDatasafeService.getStorageService().list(rootLocation)) {
            Optional<AbsoluteLocation<ResolvedResource>> first = absoluteLocationStream.filter(el -> el.location().toASCIIString().contains(path)).findFirst();
            if (!first.isPresent()) {
                throw new TestException("expeceted absoluteLocatinn stream to have at least one element");
            }

            try (InputStream read = simpleDatasafeService.getStorageService().read(first.get())) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(read, writer, UTF_8);
                assertFalse(writer.toString().equals(content));
            }
        }
        simpleDatasafeService.destroyUser(userIDAuth);
    }

    @Nullable
    @SneakyThrows
    private AbsoluteLocation<PrivateResource> getPrivateResourceAbsoluteLocation(DFSCredentials dfsCredentials) {
        if (dfsCredentials instanceof FilesystemDFSCredentials) {
            String root = ((FilesystemDFSCredentials) dfsCredentials).getRoot();
            Path listpath = FileSystems.getDefault().getPath(root);
            return new AbsoluteLocation<>(BasePrivateResource.forPrivate(listpath.toUri()));
        }
        if (dfsCredentials instanceof AmazonS3DFSCredentials) {
            AmazonS3DFSCredentials a = (AmazonS3DFSCredentials) dfsCredentials;
            return new AbsoluteLocation<>(BasePrivateResource.forPrivate(new URI(a.getUrl() + "/" + a.getRootBucket())));
        }
        throw new TestException("NYI");
    }

    static class TestException extends RuntimeException {
        public TestException(String message) {
            super(message);
        }
    }
}
