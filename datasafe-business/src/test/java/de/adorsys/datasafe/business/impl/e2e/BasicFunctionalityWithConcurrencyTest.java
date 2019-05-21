package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.e2e.metrtics.SaveTestRecord;
import de.adorsys.datasafe.business.impl.e2e.metrtics.TestMetricCollector;
import de.adorsys.datasafe.business.impl.e2e.metrtics.TestRecord;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.net.URI;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.datasafe.business.api.types.action.ListRequest.forDefaultPrivate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class BasicFunctionalityWithConcurrencyTest extends WithStorageProvider {

    private static final String MESSAGE_ONE = "Hello here 1";
    private static final String MESSAGE_TWO = "Hello here 2";
    private static final String FOLDER = "folder1";
    private static final String PRIVATE_FILE = "secret.txt";

    private static final int NUMBER_OF_TEST_USERS = 2;//10;
    private static final int NUMBER_OF_TEST_FILES = 5;//100;
    private static final int EXPECTED_NUMBER_OF_FILES_PER_USER = NUMBER_OF_TEST_FILES;

    protected StorageService storage;
    protected URI location;

    private TestMetricCollector<TestRecord> metricCollector = new TestMetricCollector<>();

    @SneakyThrows
    @ParameterizedTest(name = "Run #{index} service storage: {0} with data size: {1} bytes and {2} threads.")
    @MethodSource("differentThreadsTestOptions")
    public void writeToPrivateListPrivateInDifferentThreads(WithStorageProvider.StorageDescriptor descriptor,
                                                            int size, int poolSize) {
        this.location = descriptor.getLocation();
        this.services = descriptor.getDocusafeServices();
        this.storage = descriptor.getStorageService();

        String testFile =  "./test.txt";
        generateTestFile(testFile, size);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);

        CountDownLatch holdingLatch = new CountDownLatch(1);
        CountDownLatch finishHoldingLatch = new CountDownLatch(NUMBER_OF_TEST_USERS * NUMBER_OF_TEST_FILES);

        String checksumOfOriginTestFile;
        try(FileInputStream input = new FileInputStream(new File(testFile))) {
            checksumOfOriginTestFile = checksum(input);
        }

        log.trace("*** Starting write threads ***");
        for (int i = 0; i < NUMBER_OF_TEST_USERS; i++) {
            long userRegisterTime = System.currentTimeMillis();
            String userName = "john_" + i;
            executor.execute(() -> {
                UserIDAuth user = registerUser(userName, location);
                long durationUserCreation = System.currentTimeMillis() - userRegisterTime;

                metricCollector.addRegisterRecords(user.getUserID().getValue(), new TestRecord(durationUserCreation));

                log.debug("Registered user: {} in {}ms", user.getUserID().getValue(), durationUserCreation);

                createFileForUserParallelly(executor, holdingLatch, finishHoldingLatch,
                        testFile, user, metricCollector, NUMBER_OF_TEST_FILES);
            });
        }
        // open latch and start all threads
        holdingLatch.countDown();

        log.trace("*** Main thread waiting for all threads ***");
        finishHoldingLatch.await();
        executor.shutdown();
        log.trace("*** All threads are finished work ***");

        log.trace("*** Starting read info saved earlier *** ");
        for (int i = 0; i < NUMBER_OF_TEST_USERS; i++) {
            UserIDAuth user = createJohnTestUser(i);

            List<AbsoluteResourceLocation<PrivateResource>> resourceList = services.privateService().list(
                    forDefaultPrivate(user, "./")).collect(Collectors.toList());
            log.debug("Read files for user: " + user.getUserID().getValue());

            assertThat(resourceList.size()).isEqualTo(EXPECTED_NUMBER_OF_FILES_PER_USER);

            resourceList.forEach(item -> {
                assertEquals(checksumOfOriginTestFile, calculateDecryptedContentChecksum(user, item));
            });
        }

        metricCollector.setDataSize(size);
        metricCollector.setStorageType(storage.getClass().getSimpleName());
        metricCollector.setNumberOfThreads(poolSize);
        metricCollector.writeToJSON();//json files in target folder
    }

    private void createFileForUserParallelly(ThreadPoolExecutor executor, CountDownLatch holdingLatch,
                                             CountDownLatch finishHoldingLatch, String testFilePath,
                                             UserIDAuth user,
                                             TestMetricCollector<TestRecord> metrics, int numberOfTestFiles) {
        AtomicInteger counter = new AtomicInteger();
        String remotePath = "folder2";

        for (int j = 0; j < numberOfTestFiles; j++) {
            executor.execute(() -> {
                try {
                    holdingLatch.await();
                    Instant startSaving = Instant.now();

                    Thread.currentThread().setName(user.getUserID().getValue());

                    String filePath = remotePath + "/" + counter.incrementAndGet() + ".txt";

                    log.debug("Saving file: {}", filePath);
                    writeDataToFileForUser(user, filePath, testFilePath, finishHoldingLatch);

                    long durationOfSavingFile = Duration.between(startSaving, Instant.now()).toMillis();
                    metrics.addSaveRecord(
                                user.getUserID().getValue(),
                                SaveTestRecord
                                        .builder()
                                        .duration(durationOfSavingFile)
                                        .threadName(Thread.currentThread().getName())
                                        .build()
                    );
                    log.debug("Save file in {} ms", durationOfSavingFile);
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

    private static void generateTestFile(String testFilePath, int testFileSizeInBytes) {
        RandomAccessFile originTestFile = null;
        try {
            originTestFile = new RandomAccessFile(testFilePath, "rw");
            MappedByteBuffer out = originTestFile.getChannel()
                    .map(FileChannel.MapMode.READ_WRITE, 0, testFileSizeInBytes);

            for (int i = 0; i < testFileSizeInBytes; i++) {
                out.put((byte) 'x');
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    @ValueSource
    protected static Stream<Arguments> differentThreadsTestOptions() {
        Map<String, StorageDescriptor> storageDescriptorMap = storagesMap();
        List<Arguments> arguments = new ArrayList<>();

        storageDescriptorMap.values().forEach(storageDescriptor -> {
            //30kb - 4 threads pool size
            arguments.add(Arguments.of(storageDescriptor, 1024 * 30, 4));
            //30kb - 8 threads pool size
            arguments.add(Arguments.of(storageDescriptor, 1024 * 30, 8));
            //60kb - 4 threads pool size
            arguments.add(Arguments.of(storageDescriptor, 1024 * 60, 4));
            //60kb - 8 threads pool size
            arguments.add(Arguments.of(storageDescriptor, 1024 * 60, 8));
            //5Mb - 4 threads pool size
            arguments.add(Arguments.of(storageDescriptor, 1024 * 1024 * 5, 4));
            //5Mb - 8 threads pool size
            arguments.add(Arguments.of(storageDescriptor, 1024 * 1024 * 5, 8));
        });
        return arguments.stream();
    }

    @AfterAll
    public static void cleanUpTestFile() throws IOException {
        Files.delete(Paths.get("./test.txt"));

        log.info("Temporary test file was deleted");
    }

}
