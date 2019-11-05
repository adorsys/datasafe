package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import de.adorsys.keymanagement.api.config.keystore.KeyStoreConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.org.apache.commons.lang.time.StopWatch;

import java.io.InputStream;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class KeyStoreTypeCompareTest extends BaseE2ETest {
    private final static int NUMBER_WRITES = 100;
    private final static int NUMBER_READS = 100;

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("fsOnly")
    void compareKeyStoreTypes(WithStorageProvider.StorageDescriptor descriptor) {
        long tUBER = test(descriptor, "johnUber", "UBER");
        long tBCFKS = test(descriptor, "johnBCFKS", "BCFKS");
        log.info("UBER  test took: {}", tUBER);
        log.info("BCFKS test took: {}", tBCFKS);
        // We make sure, that with BCFKS it does not take longer than three times of UBER
        Assertions.assertTrue(tUBER * 3 > tBCFKS);
    }


    @SneakyThrows
    long test(WithStorageProvider.StorageDescriptor descriptor, String userName, String keystoreType) {
        init(descriptor, keystoreType);
        UserID user = new UserID(userName);
        assertThat(profileRetrievalService.userExists(user)).isFalse();
        john = registerUser(user.getValue(), ReadKeyPasswordTestFactory.getForString("john"));
        assertThat(profileRetrievalService.userExists(user)).isTrue();

        String filename = "root.txt";
        String content = "affe";

        StopWatch timer = new StopWatch();
        timer.start();

        for (int i = 0; i < NUMBER_WRITES; i++) {
            log.debug("write file for the {} time", i);
            try (OutputStream os = writeToPrivate
                    .write(WriteRequest.forDefaultPrivate(john, filename))) {
                os.write(content.getBytes());
            }
        }

        for (int i = 0; i < NUMBER_READS; i++) {
            log.debug("read file for the {} time", i);
            try (InputStream is = readFromPrivate
                    .read(ReadRequest.forDefaultPrivate(john, filename))) {
                assertThat(is).hasContent(content);
            }
        }

        timer.stop();

        long diff = timer.getTime();

        log.info("TIME TOOK {} MILLISECS", diff);
        return diff;
    }

    private void init(StorageDescriptor descriptor, String keystoreType) {
        DefaultDatasafeServices datasafeServices = DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(descriptor.getLocation(), new ReadStorePassword("PAZZWORT")))
                .encryption(
                        EncryptionConfig.builder()
                                .keystore(KeyStoreConfig.builder().type(keystoreType).build())
                                .build()
                )
                .storage(descriptor.getStorageService().get())
                .build();
        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);
    }
}

