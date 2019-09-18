package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.io.InvalidCipherTextIOException;
import org.cryptomator.siv.UnauthenticCiphertextException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.io.ByteStreams;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This test validates that unauthorized filename and encrypted document content is prohibited - so
 * attacker is unable to modify encrypted text without being detected.
 */
@Slf4j
class DataTamperingResistanceTest extends BaseE2ETest {

    private static final String TEXT = "Tampering test";
    private static final String FILENAME = "my_file.txt";

    @BeforeEach
    void prepare() {
        init();
        registerJohnAndJane();
    }

    @Test
    @SneakyThrows
    void testPrivateDocumentContentTamperResistance() {
        try (OutputStream os = writeToPrivate.write(WriteRequest.forDefaultPrivate(jane, FILENAME))) {
            os.write(TEXT.getBytes(StandardCharsets.UTF_8));
        }

        AbsoluteLocation<ResolvedResource> privateFile = getFirstFileInPrivate(jane);
        tamperFileByReplacingOneByteOfEncryptedMessage(privateFile);

        try (InputStream is = readFromPrivate.read(ReadRequest.forDefaultPrivate(jane, FILENAME))) {
            assertThrows(
                    InvalidCipherTextIOException.class,
                    () -> ByteStreams.copy(is, ByteStreams.nullOutputStream())
            );
        }
    }

    @Test
    @SneakyThrows
    void testInboxDocumentContentTamperResistance() {
        try (OutputStream os = writeToInbox.write(
                WriteRequest.forDefaultPublic(Collections.singleton(john.getUserID()), FILENAME))
        ) {
            os.write(TEXT.getBytes(StandardCharsets.UTF_8));
        }

        AbsoluteLocation<ResolvedResource> inboxFile = getFirstFileInInbox(john);
        tamperFileByReplacingOneByteOfEncryptedMessage(inboxFile);

        try (InputStream is = readFromInbox.read(ReadRequest.forDefaultPrivate(john, FILENAME))) {
            assertThrows(
                    InvalidCipherTextIOException.class,
                    () -> ByteStreams.copy(is, ByteStreams.nullOutputStream())
            );
        }
    }

    @Test
    @SneakyThrows
    void testPrivateDocumentPathTamperResistance() {
        try (OutputStream os = writeToPrivate.write(WriteRequest.forDefaultPrivate(jane, FILENAME))) {
            os.write(TEXT.getBytes(StandardCharsets.UTF_8));
        }

        AbsoluteLocation<ResolvedResource> privateFile = getFirstFileInPrivate(jane);
        tamperFilenameByReplacingOneCharOfPath(privateFile);

        assertThrows(
                UnauthenticCiphertextException.class,
                () -> listPrivate.list(ListRequest.forDefaultPrivate(jane, ""))
                        .forEach(it -> log.info("{}", it.location())) // consume stream
        );
    }

    private void init() {
        StorageDescriptor descriptor = fs();
        DefaultDatasafeServices datasafeServices = DatasafeServicesProvider
                .defaultDatasafeServices(descriptor.getStorageService().get(), descriptor.getLocation());
        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);
    }

    @SneakyThrows
    private void tamperFilenameByReplacingOneCharOfPath(AbsoluteLocation<ResolvedResource> privateFile) {
        // tamper the file by replacing 1 byte in it
        Path physicalFile = Paths.get(privateFile.getResource().location().asURI());
        byte[] privateBytes = Files.readAllBytes(physicalFile);
        String pathAsString = physicalFile.toAbsolutePath().toString();
        pathAsString = pathAsString.substring(0, pathAsString.length() - 1)
                + randomChar(pathAsString.charAt(pathAsString.length() - 1));
        Files.write(Paths.get(pathAsString), privateBytes, StandardOpenOption.CREATE);
    }

    @SneakyThrows
    private void tamperFileByReplacingOneByteOfEncryptedMessage(AbsoluteLocation<ResolvedResource> privateFile) {
        // tamper the file by replacing 1 byte in it
        Path physicalFile = Paths.get(privateFile.getResource().location().asURI());
        byte[] privateBytes = Files.readAllBytes(physicalFile);
        // this should 'approximately' end at encrypted text
        privateBytes[privateBytes.length - TEXT.length()] =
                randomByte(privateBytes[privateBytes.length - TEXT.length()]);
        Files.write(physicalFile, privateBytes, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private char randomChar(char notEqualTo) {
        char result;
        do {
            result = (char) (ThreadLocalRandom.current().nextInt(26) + 'a');
        } while (result == notEqualTo);

        return result;
    }

    private byte randomByte(byte notEqualTo) {
        byte[] resultArray = new byte[1];
        do {
            ThreadLocalRandom.current().nextBytes(resultArray);
        } while (notEqualTo == resultArray[0]);

        return resultArray[0];
    }
}
