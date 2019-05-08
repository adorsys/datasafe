package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.*;
import java.net.URI;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Few users save data in different threads.
 *
 * @author yatsenko-ihor
 */
@Slf4j
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
public class MultiUserInteractionE2ETest extends FsTest {

    private static final String PRIVATE_FILE_PATH = "./";
    private static final String MESSAGE_ONE = "hello";
    public static final int NUMBER_OF_USERS = 2;//10;
    public static final int NUMBER_OF_FILES = 3;//100;

    enum InteractionOperation {
        SAVE,
        SHARE,
        DELETE,
        //MOVE
    }

    List<InteractionOperation> user1Ops = Arrays.asList(
            InteractionOperation.SAVE, InteractionOperation.SHARE, InteractionOperation.DELETE);

    List<InteractionOperation> user2Ops = Arrays.asList(
            InteractionOperation.SAVE, InteractionOperation.SHARE, InteractionOperation.DELETE);

    CountDownLatch fileSaveCountDown = new CountDownLatch(NUMBER_OF_USERS * NUMBER_OF_FILES);
/*
    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
                // Specify which benchmarks to run.
                // You can be more specific if you'd like to run only one benchmark per test.
                .include(this.getClass().getName() + ".*")
                // Set the following options as needed
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(2)
                .threads(2)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                //.addProfiler(WinPerfAsmProfiler.class)
                .build();

        new Runner(opt).run();
    }*/


    @Test
    public void runBenchmarks() throws Exception {
        Options options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(6)
                .threads(1)
                .measurementIterations(6)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        Collection<RunResult> runResults = new Runner(options).run();
        assertFalse(runResults.isEmpty());
        for(RunResult runResult : runResults) {
            System.out.println(runResult.toString());
        }

    }


    @Test
    @SneakyThrows
    @Benchmark
    public void WriteToPrivateListPrivateInDifferentThreads() {
        String path = "./";

        log.trace("*** Starting write threads ***");
        for (int i = 0; i < NUMBER_OF_USERS; i++) {
            UserIDAuth john = registerUser("_john_" + i, URI.create("./"));
            log.debug("Registered user: {}", john.getUserID().getValue());

            AtomicInteger counter = new AtomicInteger();

            for (int j = 0; j < NUMBER_OF_FILES; j++) {
                new Thread(() -> {
                    try {
                        log.trace("Start thread: {} for user: {}",
                                Thread.currentThread().getName(), john.getUserID().getValue());

                        String filePath = path + "/" + counter.incrementAndGet() + ".txt";

                        log.debug("Saving file: {}", filePath);
                        writeTextToFileForUser(john, filePath, MESSAGE_ONE, fileSaveCountDown);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }

        log.trace("*** Main thread waiting for all threads ***");
        fileSaveCountDown.await();
        log.trace("*** All threads are finished work ***");

        log.trace("*** Starting read info saved earlier *** ");
        for (int i = 0; i < NUMBER_OF_USERS; i++) {
            UserIDAuth user = createJohnTestUser(i);

            List<PrivateResource> resourceList = services.privateService().list(new ListRequest<>(user, "./")).collect(Collectors.toList());
            log.debug("Read files for user: " + user.getUserID().getValue());

            assertThat(resourceList.size()).isEqualTo(100);
            resourceList.forEach(item -> {
                String content = extractFileContent(user, item);

                log.debug("Content: " + content);
                assertThat(content).isEqualTo(MESSAGE_ONE);
            });
        }}

    @SneakyThrows
    public String checksum(File input) {
        try (InputStream in = new FileInputStream(input)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] block = new byte[4096];
            int length;
            while ((length = in.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            return Hex.toHexString(digest.digest());
        }
    }

    private void generateTestFile(String testFilePath, int testFileSizeInBytes) throws IOException {
        RandomAccessFile originTestFile = new RandomAccessFile(testFilePath, "rw");
        MappedByteBuffer out = originTestFile.getChannel()
                .map(FileChannel.MapMode.READ_WRITE, 0, testFileSizeInBytes);

        for (int i = 0; i < testFileSizeInBytes; i++) {
            out.put((byte) 'x');
        }
    }

    @Test
    @SneakyThrows
    // TODO: fix two files with the same name
    public void testOneUserWriteTwoBigFileWithSameNameToPrivateStorage() {
        int testFileSizeInBytes = 1024 * 1024 * 1;//128; //128Mb

        String testFilePath = "./test.txt";
        generateTestFile(testFilePath, testFileSizeInBytes);

        UserIDAuth jack = registerUser("Jack", URI.create("./"));

        CountDownLatch countDown = new CountDownLatch(1);

        int loopCounter = 1;
        while (loopCounter > 0) {
            new Thread(() -> {
                OutputStream write = services.privateService()
                        .write(WriteRequest.forPrivate(jack, "./"));
                log.debug("Path for test file: {}", Paths.get(testFilePath));
                try {
                    Files.copy(Paths.get(testFilePath), write);
                    write.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                countDown.countDown();

            }).start();
            loopCounter--;
        }
        countDown.await();

        List<PrivateResource> files = services.privateService().list(new ListRequest<>(jack, "./"))
                .collect(Collectors.toList());

        AtomicInteger counter = new AtomicInteger();
        files.forEach(item -> {
            try {
                InputStream read = services.privateService().read(new ReadRequest<>(jack, item));
                File file = new File("./test_res_" + counter.incrementAndGet() + ".txt");
                file.createNewFile();
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));

                ByteStreams.copy(read, outputStream);
                read.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        assertThat(files.size()).isEqualTo(2);
        removeUser(jack);
    }

    private UserIDAuth createJohnTestUser(int i) {
        UserIDAuth userAuth = new UserIDAuth();
        UserID userName = new UserID("_john_" + i);
        userAuth.setUserID(userName);
        userAuth.setReadKeyPassword(new ReadKeyPassword("secure-password " + userName.getValue()));

        return userAuth;
    }

    @SneakyThrows
    private String extractFileContent(UserIDAuth john, PrivateResource privateResource) {
        InputStream read = services.privateService().read(new ReadRequest<>(john, privateResource));
        OutputStream data = new ByteArrayOutputStream();

        ByteStreams.copy(read, data);

        read.close();
        data.close();

        return data.toString();
    }

    private void writeTextToFileForUser(UserIDAuth john, String filePath, String msg, CountDownLatch startSignal) throws IOException {
        WriteRequest<UserIDAuth, PrivateResource> writeRequest = WriteRequest.<UserIDAuth, PrivateResource>builder()
                .owner(john)
                .location(DefaultPrivateResource.forPrivate(URI.create(filePath)))
                .build();

        OutputStream write = services.privateService().write(writeRequest);
        write.write(msg.getBytes());
        write.close();

        startSignal.countDown();
    }

   /* @AfterEach
    void cleanUp() {
        for (int i = 0; i < NUMBER_OF_USERS; i++) {
            removeUser(createJohnTestUser(i));
        }

    }*/

}
