package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.ResolvedResource;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.adorsys.datasafe.business.api.types.action.ListRequest.forDefaultPrivate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class BasicFunctionalityWithConcurrencyTest extends WithStorageProvider {

    private static final int TIMEOUT_S = 10;

    private static final String MESSAGE_ONE = "Hello here 1";
    private static final String MESSAGE_TWO = "Hello here 2";
    private static final String FOLDER = "folder1";
    private static final String PRIVATE_FILE = "secret.txt";

    private static final int NUMBER_OF_TEST_USERS = 5;//10;
    private static final int NUMBER_OF_TEST_FILES = 10;//100;
    private static final int EXPECTED_NUMBER_OF_FILES_PER_USER = NUMBER_OF_TEST_FILES;
    private static final int FILE_SIZE_IN_KB = 1024 * 30; //30Kb

    protected StorageService storage;
    protected URI location;

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("storages")
    void writeToPrivateListPrivateInDifferentThreads(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        byte[] testData = generateTestData(FILE_SIZE_IN_KB);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        CountDownLatch holdingLatch = new CountDownLatch(1);
        CountDownLatch finishHoldingLatch = new CountDownLatch(NUMBER_OF_TEST_USERS * NUMBER_OF_TEST_FILES);

        String checksumOfOriginTestFile = checksum(new ByteArrayInputStream(testData));

        log.trace("*** Starting write threads ***");
        for (int i = 0; i < NUMBER_OF_TEST_USERS; i++) {
            long userRegisterTime = System.currentTimeMillis();
            UserIDAuth user = registerUser("john_" + i, location);
            loadReport.add("Register user " + user.getUserID().getValue() + ": " +
                    (System.currentTimeMillis() - userRegisterTime) + "ms");
            log.debug("Registered user: {}", user.getUserID().getValue());

            createFileForUserParallelly(executor, holdingLatch, finishHoldingLatch, testData, user);

            // open latch and start all threads
            holdingLatch.countDown();
        }

        log.trace("*** Main thread waiting for all threads ***");
        finishHoldingLatch.await(TIMEOUT_S, TimeUnit.SECONDS);
        executor.shutdown();
        log.trace("*** All threads are finished work ***");

        log.trace("*** Starting read info saved earlier *** ");
        for (int i = 0; i < NUMBER_OF_TEST_USERS; i++) {
            UserIDAuth user = createJohnTestUser(i);

            List<AbsoluteLocation<ResolvedResource>> resourceList = listPrivate.list(
                    forDefaultPrivate(user, "./")).collect(Collectors.toList());
            log.debug("Read files for user: " + user.getUserID().getValue());

            assertThat(resourceList.size()).isEqualTo(EXPECTED_NUMBER_OF_FILES_PER_USER);

            resourceList.forEach(item -> {
                assertEquals(checksumOfOriginTestFile, calculateDecryptedContentChecksum(user, item));
            });
        }
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("storages")
    void testCrossReadWriteOperationsBetweenUsersInboxPrivateComponents(
            WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        List<String> prefixes = Arrays.asList("A_", "B_", "C_", "D_");
        List<String> testPath = new ArrayList<>();
        prefixes.forEach(prefix -> {
            String filePath = FOLDER + "/" + prefix + PRIVATE_FILE;
            testPath.add(filePath);
        });

        registerJohnAndJane(descriptor.getLocation());

        //initial record in John private
        sendToInbox(john.getUserID(), FOLDER, MESSAGE_ONE);
        sendToInbox(jane.getUserID(), FOLDER, MESSAGE_TWO);

        AtomicInteger counter = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(4);
        for (int i = 0; i < 2; i++) {
            executor.execute(() -> readOriginUserInboxAndWriteToTargetUserPrivate(
                    john,
                    jane,
                    countDownLatch,
                    prefixes.get(counter.getAndIncrement())
            ));
            executor.execute(() -> readOriginUserInboxAndWriteToTargetUserPrivate(
                    jane,
                    john,
                    countDownLatch,
                    prefixes.get(counter.getAndIncrement())
            ));
        }
        countDownLatch.await(TIMEOUT_S, TimeUnit.SECONDS);
        executor.shutdown();

        List<AbsoluteLocation<ResolvedResource>> privateJohnFiles = getAllFilesInPrivate(john);
        List<AbsoluteLocation<ResolvedResource>> privateJaneFiles = getAllFilesInPrivate(jane);

        List<String> expectedData = Arrays.asList(MESSAGE_ONE, MESSAGE_TWO);

        validateUserPrivateStorage(testPath, privateJohnFiles, expectedData, john);

        validateUserPrivateStorage(testPath, privateJaneFiles, expectedData, jane);

    }

    private void init(WithStorageProvider.StorageDescriptor descriptor) {
        DefaultDatasafeServices datasafeServices = DatasafeServicesProvider
                .defaultDatasafeServices(descriptor.getStorageService().get(), descriptor.getLocation());
        initialize(datasafeServices);

        this.location = descriptor.getLocation();
        this.storage = descriptor.getStorageService().get();
    }

    private void createFileForUserParallelly(ThreadPoolExecutor executor, CountDownLatch holdingLatch,
                                             CountDownLatch finishHoldingLatch, byte[] bytes,
                                             UserIDAuth john) {
        AtomicInteger counter = new AtomicInteger();
        String remotePath = "folder2";

        for (int j = 0; j < NUMBER_OF_TEST_FILES; j++) {
            executor.execute(() -> {
                try {
                    holdingLatch.await(TIMEOUT_S, TimeUnit.SECONDS);

                    long measurementOfWritingTextToFile = System.currentTimeMillis();
                    Thread.currentThread().setName(john.getUserID().getValue());

                    String filePath = remotePath + "/" + counter.incrementAndGet() + ".txt";

                    log.debug("Saving file: {}", filePath);
                    writeDataToFileForUser(john, filePath, bytes, finishHoldingLatch);

                    loadReport.add(Thread.currentThread().getName() + " write data to user " + john.getUserID().getValue() + ": " +
                            (System.currentTimeMillis() - measurementOfWritingTextToFile) + "ms");
                } catch (IOException | InterruptedException e) {
                    fail(e);
                }
            });
        }
    }

    private String calculateDecryptedContentChecksum(UserIDAuth user,
                                                     AbsoluteLocation<ResolvedResource> item) {
        try {
            InputStream decryptedFileStream = readFromPrivate.read(
                    ReadRequest.forPrivate(user, item.getResource().asPrivate()));
            String checksumOfDecryptedTestFile = checksum(decryptedFileStream);
            decryptedFileStream.close();
            return checksumOfDecryptedTestFile;
        } catch (IOException e) {
            fail(e);
        }

        return "";
    }

    @SneakyThrows
    private String checksum(InputStream input) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] block = new byte[4096];
        int length;
        while ((length = input.read(block)) > 0) {
            digest.update(block, 0, length);
        }
        return Hex.toHexString(digest.digest());
    }

    private void validateUserPrivateStorage(List<String> testPath,
                                            List<AbsoluteLocation<ResolvedResource>> privateJohnFiles,
                                            List<String> expectedData, UserIDAuth john) {
        assertThat(privateJohnFiles).hasSize(2);
        privateJohnFiles.forEach(item -> {
            String data = readPrivateUsingPrivateKey(john, item.getResource().asPrivate());
            assertThat(testPath).contains(item.getResource().asPrivate().decryptedPath().getPath());
            assertThat(expectedData.contains(data)).isTrue();
        });
    }

    private void readOriginUserInboxAndWriteToTargetUserPrivate(UserIDAuth originUser, UserIDAuth targetUser,
                                                                CountDownLatch countDownLatch, String prefixes) {
        AbsoluteLocation<ResolvedResource> inbox = getFirstFileInInbox(originUser);

        String result = readInboxUsingPrivateKey(originUser, inbox.getResource().asPrivate());

        writeDataToPrivate(targetUser, FOLDER + "/" + prefixes + PRIVATE_FILE, result);

        countDownLatch.countDown();
    }

    @AfterAll
    @SneakyThrows
    static void exportReport() {
        loadReport.stream().sorted().forEach(log::info);
    }

    private static byte[] generateTestData(int testFileSizeInBytes) {
        byte[] data = new byte[testFileSizeInBytes];

        IntStream.range(0, testFileSizeInBytes).forEach(it -> data[it] = 'x');

        return data;
    }
}
