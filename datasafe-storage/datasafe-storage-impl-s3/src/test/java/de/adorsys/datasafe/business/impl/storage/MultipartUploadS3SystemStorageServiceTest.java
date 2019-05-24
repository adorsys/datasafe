package de.adorsys.datasafe.business.impl.storage;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.BasePrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.shaded.org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class MultipartUploadS3SystemStorageServiceTest extends S3SystemStorageServiceTest {

    @TempDir
    protected Path tempDir;

    @Test
    @SneakyThrows
    void write() {
        String testFileName = tempDir.toString() + "/test.txt";

        int loadS3TestFileSizeMb = 100;//1024;//1;

        String loadS3TestfileSizeMb = System.getenv("LOAD_S3_TESTFILE_SIZE_MB");
        if(loadS3TestfileSizeMb != null && loadS3TestfileSizeMb.length() != 0) {
            loadS3TestFileSizeMb = Integer.parseInt(loadS3TestfileSizeMb);
        }

        generateTestFile(testFileName, loadS3TestFileSizeMb);

        String testFileChecksum = calculateChecksumOfTestFile(testFileName);

        AbsoluteLocation<PrivateResource> privateLocation = new AbsoluteLocation<>(
                BasePrivateResource.forPrivate(URI.create("s3://" + bucketName + "/bigfile.txt")));

        try (OutputStream os = storageService.write(privateLocation)) {
            int _1mb = 1024 * 1024;
            for (int i = 1; i <= loadS3TestFileSizeMb; i++) {
                os.write(generateArrayWithSize(_1mb));
                log.trace("Wrote {}mb of test file from {}mb", _1mb * i, loadS3TestFileSizeMb);
            }
        }

        try(InputStream is = storageService.read(privateLocation)) {
            String checksumOfFileFromS3 = checksum(is);

            assertThat(testFileChecksum).isEqualTo(checksumOfFileFromS3);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    private void generateTestFile(String testFileName, int loadS3TestFileSizeMb) {
        log.info("Starting write {} Mb file into {}", loadS3TestFileSizeMb, tempDir.toString());
        try(FileOutputStream stream = new FileOutputStream(testFileName)) {
            int _1mb = 1024 * 1024;
            for (int i = 1; i <= loadS3TestFileSizeMb; i++) {
                stream.write(generateArrayWithSize(_1mb));
                log.trace("Wrote {}mb of test file from {}mb", _1mb * i, loadS3TestFileSizeMb);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private String calculateChecksumOfTestFile(String testFileName) {
        try(FileInputStream is = new FileInputStream(testFileName)) {
            return checksum(is);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }

        return "";
    }

    private byte[] generateArrayWithSize(int _5mb) {
        byte[] data = new byte[_5mb];

        IntStream.range(0, _5mb).forEach(it -> data[it] = 'x');
        return data;
    }

    @SneakyThrows
    private String checksum(InputStream input) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] block = new byte[1024 * 8];
        int length;
        int bufferCounter = 0;
        while ((length = input.read(block)) > 0) {
            digest.update(block, 0, length);

            log.debug("Counter checksum calculation: " + (bufferCounter++));
        }
        return Hex.toHexString(digest.digest());
    }
}
