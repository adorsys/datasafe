package de.adorsys.datasafe.business.impl.e2e;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDocusafeServices;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Few users share big data between each other.
 *
 * @author yatsenko-ihor
 */
public class MultiUserInteractionE2ETest extends FsTest {

    private static final String PRIVATE_FILE_PATH = "./";
    private static final String MESSAGE_ONE = "hello";

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


    @BeforeEach
    void init(@TempDir Path location) {

        this.services = DaggerDefaultDocusafeServices
                .builder()
                .storageList(storage::list)
                .storageRead(storage::read)
                .storageWrite(storage::write)
                .build();
    }

    @Test
    @SneakyThrows
    public void test() {
        String path = "test";

        for (int i = 0; i < 10; i++) {
            UserIDAuth john = registerUser("john_" + i);

            AtomicInteger counter = new AtomicInteger();
            CountDownLatch fileSaveCountDown = new CountDownLatch(10);
            for (int j = 0; j < 100; j++) {
                new Thread(() -> {
                    try {
                        String filePath = path + "/" + counter.incrementAndGet() + ".txt";
                        System.out.println(filePath);
                        writeTextToFileForUser(john, filePath, MESSAGE_ONE, fileSaveCountDown);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

            fileSaveCountDown.await();
        }

        for (int i = 0; i < 10; i++) {
            UserIDAuth user = createTestUser(i);

            List<PrivateResource> resourceList = services.privateService().list(new ListRequest<>(user, "./")).collect(Collectors.toList());
            System.out.println("++++++++++++++++++++++++++++++");
            System.out.println("User: " + user.getUserID().getValue());

            assertThat(resourceList.size()).isEqualTo(100);
            resourceList.forEach(item -> {
                String content = extractFileContent(user, item);
                System.out.println("Content: " + content);
                assertThat(content).isEqualTo(MESSAGE_ONE);
            });
        }
/*
        List<PrivateResource> list = services.privateService().list(new ListRequest<>(
                john, DefaultPrivateResource.forPrivate(URI.create("./")))).collect(Collectors.toList());

        System.out.println("=========================");
        list.forEach(item -> {
            System.out.println(item.resolve(item));
            System.out.println(extractFileContent(john, item));
            assertThat(extractFileContent(john, item)).isEqualTo(MESSAGE_ONE);
        })*/
        ;


        //assertThat(list.size()).isEqualTo(10);
    }

    private UserIDAuth createTestUser(int i) {
        UserIDAuth userAuth = new UserIDAuth();
        UserID userName = new UserID("john_" + i);
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

}
