package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class KeyStoreTypeCompareTest extends BaseE2ETest {
    private final static int NUMBER_WRITES = 100;
    private final static int NUMBER_READS = 100;

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("fsOnly")
    void compareKeyStoreTypes(WithStorageProvider.StorageDescriptor descriptor) {
        long t1 = test(descriptor, "johnUber", "UBER");
        long t2 = test(descriptor, "johnBCFKS", "BCFKS");
        log.info("UBER  test took:" + t1);
        log.info("BCFKS test took:" + t2);
        // We make sure, that with BCFKS it does not take longer than three times of UBER
        Assert.assertTrue(t1 * 3 > t2);
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

        Date start = new Date();

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

        Date stop = new Date();
        long diff = stop.getTime() - start.getTime();

        log.info("TIME TOOK {} MILLISECS",diff);
        return diff;
    }

    private void init(StorageDescriptor descriptor, String keystoreType) {

        MutableEncryptionConfig mutableEncryptionConfig = new MutableEncryptionConfig();
        MutableEncryptionConfig.MutableKeyStoreCreationConfig mutableKeyStoreCreationConfig = new MutableEncryptionConfig.MutableKeyStoreCreationConfig();
        mutableKeyStoreCreationConfig.setType(keystoreType);
        mutableEncryptionConfig.setKeystore(mutableKeyStoreCreationConfig);
        mutableEncryptionConfig.getKeystore();

        DefaultDatasafeServices datasafeServices = DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(descriptor.getLocation(), new ReadStorePassword("PAZZWORT")))
                .encryption(
                        mutableEncryptionConfig.toEncryptionConfig().toBuilder()
                                .build()
                )
                .storage(descriptor.getStorageService().get())
                .build();
        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);
    }
}
