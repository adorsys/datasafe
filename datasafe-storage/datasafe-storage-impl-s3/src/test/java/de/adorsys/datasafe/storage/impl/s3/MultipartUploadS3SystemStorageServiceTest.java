package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import de.adorsys.datasafe.types.api.shared.ContentGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.shaded.com.google.common.io.ByteStreams;
import org.testcontainers.shaded.org.bouncycastle.util.encoders.Hex;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class MultipartUploadS3SystemStorageServiceTest extends S3SystemStorageServiceTest {

    @TempDir
    protected Path tempDir;

    private static final int ONE_MB_IN_BYTES = 1024 * 1024;
    private static final int ONE_MB = 1;
    private static final int DEFAULT_TEST_FILE_SIZE_MB = 10;

    @MethodSource("testFileSize")
    @ParameterizedTest(name = "Run #{index} with data size: {0} Mb")
    void testMultiPartUpload(int testFileSizeInMb) {
        int testFileSizeInBytes = testFileSizeInMb * ONE_MB_IN_BYTES;

        String testFileName = tempDir.toString() + "/test.txt";

        generateTestFile(testFileName, testFileSizeInBytes);
        log.info("Created test file {} with size {} bytes", testFileName, testFileSizeInBytes);

        AbsoluteLocation<PrivateResource> privateLocation = new AbsoluteLocation<>(
                BasePrivateResource.forPrivate(URI.create("s3://" + bucketName + "/file.txt")));

        writeTestFileToS3(testFileName, privateLocation);
        log.info("Data has been written to S3");

        assertThat(checksumOfTestFile(testFileName)).isEqualTo(checksumOfFileFromS3(privateLocation));
    }

    @ValueSource
    protected static Stream<Integer> testFileSize() {
        return Stream.of(
                ONE_MB, // 1Mb. The minimum contentSize for a multi part request is 5 MB, file with size < 5 mb uses simple output impl
                getTestFileSizeInMb() //Size from env var LOAD_S3_TEST_FILE_SIZE_MB or default value DEFAULT_TEST_FILE_SIZE_MB
        );
    }

    private static int getTestFileSizeInMb() {
        try {
            return Integer.parseInt(System.getenv("LOAD_S3_TEST_FILE_SIZE_MB"));
        } catch (NumberFormatException ex) {
            return DEFAULT_TEST_FILE_SIZE_MB;
        }
    }

    private void writeTestFileToS3(String testFilePath, AbsoluteLocation<PrivateResource> privateLocation) {
        log.info("Copy stream of test file to s3");
        try (OutputStream os = storageService.write(WithCallback.noCallback(privateLocation))) {
            try(FileInputStream is = new FileInputStream(testFilePath)) {
                ByteStreams.copy(is, os);
            }
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    private String checksumOfFileFromS3(AbsoluteLocation<PrivateResource> privateLocation) {
        try(InputStream is = storageService.read(privateLocation)) {
            return checksum(is);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
        return "";
    }

    private void generateTestFile(String testFileName, int loadS3TestFileSizeMb) {
        log.info("Starting write {} Mb file into {}", loadS3TestFileSizeMb, tempDir.toString());
        try(FileOutputStream stream = new FileOutputStream(testFileName)) {
            ByteStreams.copy(new ContentGenerator(loadS3TestFileSizeMb).generate(this.getClass().getSimpleName()), stream);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private String checksumOfTestFile(String testFileName) {
        try(FileInputStream is = new FileInputStream(testFileName)) {
            return checksum(is);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }

        return "";
    }

    @SneakyThrows
    private String checksum(InputStream input) {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] block = new byte[1024 * 8];
        int length;
        int bufferCounter = 0;
        while ((length = input.read(block)) > 0) {
            digest.update(block, 0, length);

            log.trace("Counter checksum calculation: " + (bufferCounter++));
        }
        return Hex.toHexString(digest.digest());
    }
}
