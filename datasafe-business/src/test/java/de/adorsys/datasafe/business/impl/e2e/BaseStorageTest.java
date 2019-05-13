package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.net.URI;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.business.api.types.action.ListRequest.forPrivate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@RequiredArgsConstructor
abstract class BaseStorageTest extends BaseE2ETest {

    private static final String MESSAGE_ONE = "Hello here 1";
    private static final String MESSAGE_TWO = "Hello here 2";
    private static final String FOLDER = "folder1";
    private static final String PRIVATE_FILE = "secret.txt";
    private static final String PRIVATE_FILE_PATH = FOLDER + "/" + PRIVATE_FILE;
    private static final String SHARED_FILE = "hello.txt";
    private static final String SHARED_FILE_PATH = SHARED_FILE;

    private static final int NUMBER_OF_TEST_USERS = 5;//10;
    private static final int NUMBER_OF_TEST_FILES = 10;//100;
    private static final int EXPECTED_NUMBER_OF_FILES_PER_USER = NUMBER_OF_TEST_FILES;
    private static final int FILE_SIZE_IN_KB = 1024 * 30; //30Kb

    protected StorageService storage;
    protected URI location;

    @Test
    @SneakyThrows
    public void writeToPrivateListPrivateInDifferentThreads(@TempDir Path tempDirPath) {
        String tempDir = tempDirPath.toString();
        String testFile = tempDir + "/test.txt";
        generateTestFile(testFile, FILE_SIZE_IN_KB);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        CountDownLatch holdingLatch = new CountDownLatch(1);
        CountDownLatch finishHoldingLatch = new CountDownLatch(NUMBER_OF_TEST_USERS * NUMBER_OF_TEST_FILES);

        FileInputStream input = new FileInputStream(new File(testFile));
        String checksumOfOriginTestFile = checksum(input);
        input.close();

        log.trace("*** Starting write threads ***");
        for (int i = 0; i < NUMBER_OF_TEST_USERS; i++) {
            long userRegisterTime = System.currentTimeMillis();
            UserIDAuth user = registerUser("john_" + i, location);
            loadReport.add("Register user " + user.getUserID().getValue() + ": " +
                    (System.currentTimeMillis() - userRegisterTime) + "ms");
            log.debug("Registered user: {}", user.getUserID().getValue());

            createFileForUserParallelly(executor, holdingLatch, finishHoldingLatch,
                    testFile, user);

            // open latch and start all threads
            holdingLatch.countDown();
        }

        log.trace("*** Main thread waiting for all threads ***");
        finishHoldingLatch.await();
        executor.shutdown();
        log.trace("*** All threads are finished work ***");

        log.trace("*** Starting read info saved earlier *** ");
        for (int i = 0; i < NUMBER_OF_TEST_USERS; i++) {
            UserIDAuth user = createJohnTestUser(i);

            List<AbsoluteResourceLocation<PrivateResource>> resourceList = services.privateService().list(forPrivate(user, "./")).collect(Collectors.toList());
            log.debug("Read files for user: " + user.getUserID().getValue());

            assertThat(resourceList.size()).isEqualTo(EXPECTED_NUMBER_OF_FILES_PER_USER);

            resourceList.forEach(item -> {
                assertEquals(checksumOfOriginTestFile, calculateDecryptedContentChecksum(user, item));
            });
        }
    }

    private void createFileForUserParallelly(ThreadPoolExecutor executor, CountDownLatch holdingLatch,
                                             CountDownLatch finishHoldingLatch, String testFilePath,
                                             UserIDAuth john) {
        AtomicInteger counter = new AtomicInteger();
        String remotePath = "folder2";

        for (int j = 0; j < NUMBER_OF_TEST_FILES; j++) {
            executor.execute(() -> {
                try {
                    holdingLatch.await();

                    long measurementOfWritingTextToFile = System.currentTimeMillis();
                    Thread.currentThread().setName(john.getUserID().getValue());

                    String filePath = remotePath + "/" + counter.incrementAndGet() + ".txt";

                    log.debug("Saving file: {}", filePath);
                    writeDataToFileForUser(john, filePath, testFilePath, finishHoldingLatch);

                    loadReport.add(Thread.currentThread().getName() + " write data to user " + john.getUserID().getValue() + ": " +
                            (System.currentTimeMillis() - measurementOfWritingTextToFile) + "ms");
                } catch (IOException | InterruptedException e) {
                    fail(e);
                }
            });
        }
    }

    private String calculateDecryptedContentChecksum(UserIDAuth user,
                                                     AbsoluteResourceLocation<PrivateResource> item) {
        try {
            InputStream decryptedFileStream = services.privateService().read(
                    ReadRequest.forPrivate(user, item.getResource()));
            String checksumOfDecryptedTestFile = checksum(decryptedFileStream);
            decryptedFileStream.close();
            return checksumOfDecryptedTestFile;
        } catch (IOException e) {
            fail(e);
        }

        return "";
    }

    @SneakyThrows
    public String checksum(InputStream input) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] block = new byte[4096];
        int length;
        while ((length = input.read(block)) > 0) {
            digest.update(block, 0, length);
        }
        return Hex.toHexString(digest.digest());
    }

    @Test
    @SneakyThrows
    public void testCrossReadWriteOperationsBetweenUsersInboxPrivateComponents() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        List<String> prefixes = Arrays.asList("A_", "B_", "C_", "D_");
        List<String> testPath = new ArrayList<>();
        prefixes.forEach(prefix -> {
            String filePath = FOLDER + "/" + prefix + PRIVATE_FILE;
            testPath.add(filePath);
        });

        registerJohnAndJane(location);

        //initial record in John private
        writeDataToInbox(john.getUserID(), FOLDER, MESSAGE_ONE);
        writeDataToInbox(jane.getUserID(), FOLDER, MESSAGE_TWO);

        AtomicInteger counter = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(4);
        for (int i = 0; i < 2; i++) {
            executor.execute(() -> {
                readOriginUserInboxAndWriteToTargetUserPrivate(john, jane, countDownLatch, prefixes.get(counter.getAndIncrement()));
            });
            executor.execute(() -> {
                readOriginUserInboxAndWriteToTargetUserPrivate(jane, john, countDownLatch, prefixes.get(counter.getAndIncrement()));
            });
        }
        countDownLatch.await();
        executor.shutdown();

        List<AbsoluteResourceLocation<PrivateResource>> privateJohnFiles = getAllFilesInPrivate(john);
        List<AbsoluteResourceLocation<PrivateResource>> privateJaneFiles = getAllFilesInPrivate(jane);

        List<String> expectedData = Arrays.asList(MESSAGE_ONE, MESSAGE_TWO);

        validateUserPrivateStorage(testPath, privateJohnFiles, expectedData, john);

        validateUserPrivateStorage(testPath, privateJaneFiles, expectedData, jane);

    }

    private void validateUserPrivateStorage(List<String> testPath, List<AbsoluteResourceLocation<PrivateResource>> privateJohnFiles, List<String> expectedData, UserIDAuth john) {
        assertThat(privateJohnFiles).hasSize(2);
        privateJohnFiles.forEach(item -> {
            String data = readPrivateUsingPrivateKey(john, item.getResource());
            assertThat(testPath).contains(item.getResource().decryptedPath().getPath());
            assertThat(expectedData.contains(data)).isTrue();
        });
    }

    private void readOriginUserInboxAndWriteToTargetUserPrivate(UserIDAuth originUser, UserIDAuth targetUser,
                                                                CountDownLatch countDownLatch, String prefixes) {
        AbsoluteResourceLocation<PrivateResource> inbox = getFirstFileInInbox(originUser);

        String result = readInboxUsingPrivateKey(originUser, inbox.getResource());

        writeDataToPrivate(targetUser, FOLDER + "/" + prefixes + PRIVATE_FILE, result);

        countDownLatch.countDown();
    }

    @AfterAll
    @SneakyThrows
    public static void exportReport() {

        loadReport.stream().sorted().forEach(reportRow -> {
            log.info(reportRow);
        });
    }

    private static void generateTestFile(String testFilePath, int testFileSizeInBytes) throws IOException {
        RandomAccessFile originTestFile = new RandomAccessFile(testFilePath, "rw");
        MappedByteBuffer out = originTestFile.getChannel()
                .map(FileChannel.MapMode.READ_WRITE, 0, testFileSizeInBytes);

        for (int i = 0; i < testFileSizeInBytes; i++) {
            out.put((byte) 'x');
        }
    }

}
