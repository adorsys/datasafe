package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Ignore;
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

import static de.adorsys.datasafe.business.api.types.action.ListRequest.forPrivate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Few users save data in different threads.
 *
 * @author yatsenko-ihor
 */
@Slf4j
public class MultiUserInteractionE2ETest extends FsTest {

    private static final String MESSAGE_ONE = "hello";
    public static final int NUMBER_OF_USERS = 10;
    public static final int NUMBER_OF_FILES = 100;

    CountDownLatch fileSaveCountDown = new CountDownLatch(NUMBER_OF_USERS * NUMBER_OF_FILES);

    @Test
    @SneakyThrows
    @Benchmark
    public void WriteToPrivateListPrivateInDifferentThreads() {
        String path = "folder2";

        log.trace("*** Starting write threads ***");
        for (int i = 0; i < NUMBER_OF_USERS; i++) {
            UserIDAuth john = registerUser("john_" + i, location);
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

            List<AbsoluteResourceLocation<PrivateResource>> resourceList = services.privateService().list(forPrivate(user, "./")).collect(Collectors.toList());
            log.debug("Read files for user: " + user.getUserID().getValue());

            assertThat(resourceList.size()).isEqualTo(100);
            resourceList.forEach(item -> {
                String content = extractFileContent(user, item.getResource());

                log.debug("Content: " + content);
                assertThat(content).isEqualTo(MESSAGE_ONE);
            });
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
    @Ignore
    public void testOneUserWriteTwoBigFileWithSameNameToPrivateStorage() {
        int testFileSizeInBytes = 1024 * 1024 * 128; //128Mb

        String testFilePath = "./test.txt";
        generateTestFile(testFilePath, testFileSizeInBytes);

        UserIDAuth jack = registerUser("Jack", location);

        CountDownLatch countDown = new CountDownLatch(2);

        int loopCounter = 2;
        while (loopCounter > 0) {
            new Thread(() -> {
                OutputStream write = services.privateService()
                        .write(WriteRequest.forPrivate(jack, location.getPath()));
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

        List<AbsoluteResourceLocation<PrivateResource>> files = services.privateService().list(forPrivate(jack, location.getPath()))
                .collect(Collectors.toList());

        AtomicInteger counter = new AtomicInteger();
        files.forEach(item -> {
            try {
                InputStream read = services.privateService().read(ReadRequest.forPrivate(jack, item.getResource()));
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

   /* @AfterEach
    void cleanUp() {
        for (int i = 0; i < NUMBER_OF_TEST_USERS; i++) {
            removeUser(createJohnTestUser(i));
        }

    }*/

}
