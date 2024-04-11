package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.spring.annotations.UseDatasafeSpringConfiguration;
import de.adorsys.datasafe.simple.adapter.spring.factory.SpringSimpleDatasafeServiceFactory;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringDFSCredentialProperties;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringDatasafeEncryptionProperties;
import de.adorsys.datasafe.teststorage.WithStorageProviderIT;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

@Slf4j
@ActiveProfiles("fs-withoutPathEncryption")
@UseDatasafeSpringConfiguration
public class InjectionWithoutPathEncryptionForFilesystemTest extends InjectionTest {
    @Autowired
    private SpringDFSCredentialProperties dfsCredentialProperties;

    @Autowired
    private SpringDatasafeEncryptionProperties encryptionProperties;

    private static Stream<StorageDescriptor> fsonly() {
        return Stream.of(fs());
    }

    @ParameterizedTest
    @MethodSource("fsonly")
    public void plainService(WithStorageProviderIT.StorageDescriptor descriptor) {
        log.info("descriptor is {}", descriptor.getName());
        DFSCredentials dfsCredentials = SpringPropertiesToDFSCredentialsUtil.dfsCredentials(dfsCredentialProperties);
        SpringSimpleDatasafeServiceFactory springSimpleDatasafeServiceFactory = new SpringSimpleDatasafeServiceFactory(dfsCredentials, encryptionProperties);
        SimpleDatasafeService service = springSimpleDatasafeServiceFactory.getSimpleDataSafeServiceWithSubdir("subdir");
        testWithoutPathEncryption(service, dfsCredentials);
    }

}
