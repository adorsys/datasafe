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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import org.testcontainers.shaded.com.google.common.io.ByteStreams;

import javax.crypto.AEADBadTagException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This test validates that unauthorized filename and encrypted document content is prohibited - so
 * attacker is unable to modify encrypted text without being detected.
 */
@Slf4j
class DataTamperingResistanceTest extends BaseE2ETest {

    private static final Set<Character> NOT_TO_REPLACE_IN_PATH = ImmutableSet.of('=', '/');

    // Should be long enough to dominate compared to padding
    private static final String FILE_TEXT =
            "Tampering test!!Tampering test!!Tampering test!!Tampering test!!Tampering test!!";
    // Should be long enough to dominate compared to padding
    private static final String FILENAME =
            "my_file_with_quite_a__long_na.me_na.me_na.me_na.me_na.me_na.me_na.me_na.me";
    // Should be long enough to dominate compared to padding
    private static final String DIR_AND_FILENAME =
            "my_directory_with_quite_a__long_na.me_na.me_na.me_na.me_na.me_na.me_na.me_na.me/my_file";
    // Should be long enough to dominate compared to padding
    private static final String DIR_DIR_AND_FILENAME =
            "my_directory_with_quite_a__long_na.me_na.me_na.me_na.me_na.me_na.me_na.me_na.me/deeper/my_file";

    @BeforeEach
    void prepare() {
        init();
        registerJohnAndJane();
    }

    @Test
    @SneakyThrows
    void testPrivateDocumentContentTamperResistance() {
        try (OutputStream os = writeToPrivate.write(WriteRequest.forDefaultPrivate(jane, FILENAME))) {
            os.write(FILE_TEXT.getBytes(StandardCharsets.UTF_8));
        }

        AbsoluteLocation<ResolvedResource> privateFile = getFirstFileInPrivate(jane);
        tamperFileByReplacingOneByteOfEncryptedMessage(privateFile);

        try (InputStream is = readFromPrivate.read(ReadRequest.forDefaultPrivate(jane, FILENAME))) {
            assertThatThrownBy(
                    () -> ByteStreams.copy(is, ByteStreams.nullOutputStream())
            ).isInstanceOf(InvalidCipherTextIOException.class).hasCauseInstanceOf(AEADBadTagException.class);
        }
    }

    @Test
    @SneakyThrows
    void testInboxDocumentContentTamperResistance() {
        try (OutputStream os = writeToInbox.write(
                WriteRequest.forDefaultPublic(Collections.singleton(john.getUserID()), FILENAME))
        ) {
            os.write(FILE_TEXT.getBytes(StandardCharsets.UTF_8));
        }

        AbsoluteLocation<ResolvedResource> inboxFile = getFirstFileInInbox(john);
        tamperFileByReplacingOneByteOfEncryptedMessage(inboxFile);

        try (InputStream is = readFromInbox.read(ReadRequest.forDefaultPrivate(john, FILENAME))) {
            assertThatThrownBy(
                    () -> ByteStreams.copy(is, ByteStreams.nullOutputStream())
            ).isInstanceOf(InvalidCipherTextIOException.class).hasCauseInstanceOf(AEADBadTagException.class);
        }
    }

    @ParameterizedTest(name = "{arguments}")
    @ValueSource(strings = {FILENAME, DIR_AND_FILENAME, DIR_DIR_AND_FILENAME})
    @SneakyThrows
    void testPrivateDocumentPathTamperResistance(String path) {
        try (OutputStream os = writeToPrivate.write(WriteRequest.forDefaultPrivate(jane, path))) {
            os.write(FILE_TEXT.getBytes(StandardCharsets.UTF_8));
        }

        AbsoluteLocation<ResolvedResource> privateFile = getFirstFileInPrivate(jane);
        tamperFilenameByReplacingOneCharOfPath(privateFile, path);

        assertThrows(
                UnauthenticCiphertextException.class,
                () -> {
                    try (Stream<AbsoluteLocation<ResolvedResource>> lsPrivate = listPrivate
                            .list(ListRequest.forDefaultPrivate(jane, ""))
                    ) {
                        lsPrivate.forEach(it -> log.info("{}", it.location())); // consume ls
                    }
                }
        );
    }

    private void init() {
        StorageDescriptor descriptor = fs();
        DefaultDatasafeServices datasafeServices = DatasafeServicesProvider
                .defaultDatasafeServices(descriptor.getStorageService().get(), descriptor.getLocation());
        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);
    }

    @SneakyThrows
    private void tamperFilenameByReplacingOneCharOfPath(AbsoluteLocation<ResolvedResource> privateFile,
                                                        String origPath) {
        // tamper the file by replacing 1 byte in it
        Path physicalFile = Paths.get(privateFile.getResource().location().asURI());
        byte[] privateBytes = Files.readAllBytes(physicalFile);
        String pathAsString = physicalFile.toAbsolutePath().toString();

        // this should 'approximately' be inside encrypted text
        int characterToTamper = pathAsString.length() - origPath.length() / 2;
        while (NOT_TO_REPLACE_IN_PATH.contains(pathAsString.charAt(characterToTamper))) {
            characterToTamper--;
        }

        log.info("About to tamper path `{}`", pathAsString);
        pathAsString = pathAsString.substring(0, characterToTamper - 1)
                + randomChar(pathAsString.charAt(characterToTamper))
                + pathAsString.substring(characterToTamper);
        log.info("Tampered path as `{}`", pathAsString);
        Files.createDirectories(Paths.get(pathAsString).getParent());
        Files.write(Paths.get(pathAsString), privateBytes, StandardOpenOption.CREATE);
    }

    @SneakyThrows
    private void tamperFileByReplacingOneByteOfEncryptedMessage(AbsoluteLocation<ResolvedResource> privateFile) {
        // tamper the file by replacing 1 byte in it
        Path physicalFile = Paths.get(privateFile.getResource().location().asURI());
        byte[] privateBytes = Files.readAllBytes(physicalFile);
        // this should 'approximately' be inside encrypted text
        int somewhereInEncText = privateBytes.length - FILE_TEXT.length() / 2;
        privateBytes[somewhereInEncText] = randomByte(privateBytes[somewhereInEncText]);
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
