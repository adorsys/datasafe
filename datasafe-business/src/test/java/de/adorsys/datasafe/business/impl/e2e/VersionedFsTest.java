package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.service.DaggerVersionedDocusafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDocusafeServices;
import de.adorsys.datasafe.business.impl.storage.FileSystemStorageService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class VersionedFsTest extends BaseStorageTest {

    private static final String MESSAGE_ONE = "Hello here 1";
    private static final String MESSAGE_TWO = "Hello here 2";
    private static final String MESSAGE_THREE = "Hello here 3";

    private static final String FOLDER = "folder1";
    private static final String PRIVATE_FILE = "secret.txt";
    private static final String PRIVATE_FILE_PATH = FOLDER + "/" + PRIVATE_FILE;

    private VersionedDocusafeServices versionedDocusafeServices;

    @BeforeEach
    void init(@TempDir Path location) {
        this.location = location.toUri();
        this.storage = new FileSystemStorageService(this.location);

        this.versionedDocusafeServices = DaggerVersionedDocusafeServices
                .builder()
                .config(new DFSConfig() {
                    @Override
                    public String keystorePassword() {
                        return "PAZZWORD";
                    }

                    @Override
                    public URI systemRoot() {
                        return location.toUri();
                    }
                })
                .storageList(storage)
                .storageRead(storage)
                .storageWrite(storage)
                .storageRemove(storage)
                .build();

        this.services = versionedDocusafeServices;

    }

    @Test
    void testWriteToPrivateListPrivateReadPrivateAndSendToAndReadFromInbox() {

        registerJohnAndJane(location);

        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_ONE);
        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_TWO);
        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_THREE);

        String result = readPrivateUsingPrivateKey(
                jane,
                DefaultPrivateResource.forPrivate(URI.create(PRIVATE_FILE_PATH)));

        assertThat(result).isEqualTo(MESSAGE_THREE);

        assertThat(getFirstFileInPrivate(jane).getResource().decryptedPath()).asString().contains(PRIVATE_FILE_PATH);
    }

    @Override
    @SneakyThrows
    protected void writeDataToPrivate(UserIDAuth auth, String path, String data) {
        OutputStream stream = versionedDocusafeServices.versionedPrivate().write(WriteRequest.forDefaultPrivate(auth, path));
        stream.write(data.getBytes());
        stream.close();
    }

    @Override
    @SneakyThrows
    protected String readPrivateUsingPrivateKey(UserIDAuth user, PrivateResource location) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream dataStream = versionedDocusafeServices.versionedPrivate().read(ReadRequest.forPrivate(user, location));

        ByteStreams.copy(dataStream, outputStream);
        String data = new String(outputStream.toByteArray());
        return data;
    }

    @Override
    protected AbsoluteResourceLocation<PrivateResource> getFirstFileInPrivate(UserIDAuth inboxOwner) {
        List<AbsoluteResourceLocation<PrivateResource>> files = versionedDocusafeServices.versionedPrivate().list(
                ListRequest.forDefaultPrivate(inboxOwner, "./")
        ).collect(Collectors.toList());

        log.info("{} has {} in PRIVATE", inboxOwner.getUserID().getValue(), files);
        return files.get(0);
    }
}
