package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.business.api.types.action.ListRequest.forPrivate;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
//TODD 3. Circle stream
//Change on the executors?
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
    private static final String TEST_FILE = "./target/test.txt";

    protected StorageService storage;
    protected URI location;

    @BeforeAll
    @SneakyThrows
    public static void prepare() {
        generateTestFile(FILE_SIZE_IN_KB);
    }

    @Test
    void testWriteToPrivateListPrivateReadPrivateAndSendToAndReadFromInbox() {
        registerJohnAndJane(location);

        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_ONE);

        AbsoluteResourceLocation<PrivateResource> privateJane = getFirstFileInPrivate(jane);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane.getResource());

        writeDataToInbox(john.getUserID(), SHARED_FILE_PATH, privateContentJane);

        AbsoluteResourceLocation<PrivateResource> inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn.getResource());

        assertThat(result).isEqualTo(MESSAGE_ONE);
        assertThat(privateJane.getResource().decryptedPath()).asString().isEqualTo(PRIVATE_FILE_PATH);
        assertThat(privateJane.getResource().encryptedPath()).asString().isNotEqualTo(PRIVATE_FILE_PATH);
        validateInboxStructAndEncryption(inboxJohn);
        validatePrivateStructAndEncryption(privateJane);
    }

    @SneakyThrows
    private void validateInboxStructAndEncryption(AbsoluteResourceLocation<PrivateResource> expectedInboxResource) {
        List<AbsoluteResourceLocation<PrivateResource>> inbox = listFiles(it -> it.contains(INBOX_COMPONENT));

        assertThat(inbox).hasSize(1);
        AbsoluteResourceLocation<PrivateResource> foundResource = inbox.get(0);
        assertThat(foundResource.location()).isEqualTo(expectedInboxResource.location());
        // no path encryption for inbox:
        assertThat(foundResource.toString()).contains(SHARED_FILE);
        // validate encryption on high-level:
        assertThat(storage.read(foundResource)).asString().doesNotContain(MESSAGE_ONE);
    }

    @SneakyThrows
    private void validatePrivateStructAndEncryption(AbsoluteResourceLocation<PrivateResource> expectedPrivateResource) {
        List<AbsoluteResourceLocation<PrivateResource>> privateFiles = listFiles(
                it -> it.contains(PRIVATE_FILES_COMPONENT));

        assertThat(privateFiles).hasSize(1);
        AbsoluteResourceLocation<PrivateResource> foundResource = privateFiles.get(0);
        assertThat(foundResource.location()).isEqualTo(expectedPrivateResource.location());

        // validate encryption on high-level:
        assertThat(foundResource.toString()).doesNotContain(PRIVATE_FILE);
        assertThat(foundResource.toString()).doesNotContain(FOLDER);
        assertThat(storage.read(foundResource)).asString().doesNotContain(MESSAGE_ONE);
    }

    @SneakyThrows
    private List<AbsoluteResourceLocation<PrivateResource>> listFiles(Predicate<String> pattern) {
        return storage.list(new AbsoluteResourceLocation<>(DefaultPrivateResource.forPrivate(location)))
                .filter(it -> !it.location().toString().startsWith("."))
                .filter(it -> pattern.test(it.location().toString()))
                .collect(Collectors.toList());
    }

    @Test
    @SneakyThrows
   // @Ignore
    public void WriteToPrivateListPrivateInDifferentThreads() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        CountDownLatch startHoldingLatch = new CountDownLatch(1);
        CountDownLatch finishHoldingLatch = new CountDownLatch(NUMBER_OF_TEST_USERS * NUMBER_OF_TEST_FILES);

        String path = "folder2";

        log.trace("*** Starting write threads ***");
        for (int i = 0; i < NUMBER_OF_TEST_USERS; i++) {
            long userRegisterTime = System.currentTimeMillis();
            UserIDAuth john = registerUser("john_" + i, location);
            loadReport.put("Register user " + john.getUserID().getValue(), System.currentTimeMillis() - userRegisterTime);
            log.debug("Registered user: {}", john.getUserID().getValue());

            AtomicInteger counter = new AtomicInteger();

            for (int j = 0; j < NUMBER_OF_TEST_FILES; j++) {
                executor.execute(() -> {
                    try {
                        startHoldingLatch.await();

                        long measurementOfWritingTextToFile = System.currentTimeMillis();
                        log.trace("Start thread: {} for user: {}",
                                Thread.currentThread().getName(), john.getUserID().getValue());

                        String filePath = path + "/" + counter.incrementAndGet() + ".txt";

                        log.debug("Saving file: {}", filePath);
                        writeDataToFileForUser(john, filePath, TEST_FILE, finishHoldingLatch);

                        loadReport.put(Thread.currentThread().getName() + " write data to user " + john.getUserID().getValue(), System.currentTimeMillis() - measurementOfWritingTextToFile);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }

            // open latch and start all threads
            startHoldingLatch.countDown();
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
            /*resourceList.forEach(item -> {
                String content = extractFileContent(user, item.getResource());

                log.debug("Content: " + content);
                assertThat(content).isEqualTo(generateContent(FILE_SIZE_IN_KB));
            })*/;
        }
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

        assertThat(privateJohnFiles).hasSize(2);
        privateJohnFiles.forEach(item -> {
            String data = readPrivateUsingPrivateKey(john, item.getResource());
            assertThat(testPath).contains(item.getResource().decryptedPath().getPath());
            assertThat(expectedData.contains(data)).isTrue();
        });

        assertThat(privateJaneFiles).hasSize(2);
        privateJaneFiles.forEach(item -> {
            String data = readPrivateUsingPrivateKey(jane, item.getResource());
            assertThat(testPath).contains(item.getResource().decryptedPath().getPath());
            assertThat(expectedData.contains(data)).isTrue();
        });

    }

    private void readOriginUserInboxAndWriteToTargetUserPrivate(UserIDAuth originUser, UserIDAuth targetUser,
                                                                CountDownLatch countDownLatch, String _const) {
        AbsoluteResourceLocation<PrivateResource> inbox = getFirstFileInInbox(originUser);

        String result = readInboxUsingPrivateKey(originUser, inbox.getResource());

        writeDataToPrivate(targetUser, FOLDER + "/" + _const + PRIVATE_FILE, result);

        countDownLatch.countDown();
    }

    @AfterAll
    @SneakyThrows
    public static void exportReport() {
       // Files.delete(Paths.get(TEST_FILE));

        loadReport.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(reportRow -> {
            System.out.println(reportRow.getKey() + " " + reportRow.getValue() + " millisec");
        });
    }

    private static void generateTestFile(int testFileSizeInBytes) throws IOException {
        RandomAccessFile originTestFile = new RandomAccessFile(TEST_FILE, "rw");
        MappedByteBuffer out = originTestFile.getChannel()
                .map(FileChannel.MapMode.READ_WRITE, 0, testFileSizeInBytes);

        for (int i = 0; i < testFileSizeInBytes; i++) {
            out.put((byte) 'x');
        }
    }
}
