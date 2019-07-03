package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.business.impl.e2e.WithStorageProvider;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.spring.factory.SpringSimpleDatasafeServiceFactory;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringAmazonS3DFSCredentialsProperties;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringDFSCredentialProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;


// yes, this is correct. the factory will be instantieated via spring with the filesystem
// but during runtime we create another factory with minio, right after we got the parameters from
// the descriptor
@ActiveProfiles("filesystem")

@Slf4j
public class InectionForMinioTest extends InjectionTest {

    private static Stream<StorageDescriptor> minioonly() {
        return allStorages().filter(storage -> storage.getName().equals(StorageDescriptorName.MINIO));
    }

    @ParameterizedTest
    @MethodSource("minioonly")
    public void plainService(WithStorageProvider.StorageDescriptor descriptor) {

        log.info("descriptor is " + descriptor.getName());
        descriptor.getStorageService().get();
        SpringAmazonS3DFSCredentialsProperties s3properties = new SpringAmazonS3DFSCredentialsProperties();
        s3properties.setAccesskey(descriptor.getAccessKey());
        s3properties.setSecretkey(descriptor.getSecretKey());
        s3properties.setRegion(descriptor.getRegion());
        s3properties.setUrl(descriptor.getMappedUrl());
        s3properties.setRootbucket("target");
        SpringDFSCredentialProperties properties = new SpringDFSCredentialProperties();
        properties.setAmazons3(s3properties);
        log.info("properties for miniotest created by test look:" + properties);
        DatasafeSpringBeans s = new DatasafeSpringBeans();
        SpringSimpleDatasafeServiceFactory springSimpleDatasafeServiceFactory = s.simpleDatasafeServiceFactory(properties);

        SimpleDatasafeService service = springSimpleDatasafeServiceFactory.getSimpleDataSafeServiceWithSubdir("subdir");
        testCreateUser(service);
    }
}
