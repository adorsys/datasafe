package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.business.impl.e2e.WithStorageProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

@ActiveProfiles("minio")
@Slf4j
public class InectionForMinioTest extends InjectionTest {

    private static Stream<StorageDescriptor> minioonly() {
        return  allStorages().filter(storage -> storage.getName().equals(StorageDescriptorName.MINIO));
    }

    // @Test
    @MethodSource("minioonly")
    public void plainService(WithStorageProvider.StorageDescriptor descriptor) {
        log.info("using mapped url:" + descriptor.getMappedUrl());
        testCreateUser();
    }
}
