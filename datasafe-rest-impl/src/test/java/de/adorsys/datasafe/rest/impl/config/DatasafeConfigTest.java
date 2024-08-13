package de.adorsys.datasafe.rest.impl.config;

import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@ActiveProfiles("client-credentials-test")
class DatasafeConfigTest extends BaseMockitoTest {

    private static final String BASIC_STORAGE_ANSWER = "Hello";
    private static final String BASIC_FILE_STORAGE_PATH = "tmp/profile";
    private static final String BASIC_STORAGE_PATH = "file:///tmp/profile";


    private static final String DATA_BUCKET = "databucket";
    private static final String DATA_STORAGE_ANSWER = "Hello storage";
    private static final String DATA_FILE_STORAGE_PATH = "path-to/file";
    private static final String DATA_STORAGE_PATH = "http://user:passwd@0.0.0.0:9000/eu-central-1/databucket/path-to/file";

    @Value("${AWS_BUCKET}")
    private String basicBucket;

    // bucket name is overridden by dev build environment variable
    @Autowired
    private StorageService storageService;

//    @MockBean
//    private AmazonS3 amazonS3;

    @Mock
    private S3Client s3ClientFromFactory;

    @MockBean
    private S3Factory s3Factory;

    @BeforeEach
    void prepare() {
        when(s3Factory.getClient("http://0.0.0.0:9000/", "eu-central-1", "user", "passwd"))
                .thenReturn(s3ClientFromFactory);

        GetObjectResponse objectResponseMock = GetObjectResponse.builder()
                .contentLength((long) BASIC_STORAGE_ANSWER.getBytes(UTF_8).length)
                .build();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(BASIC_STORAGE_ANSWER.getBytes(UTF_8));
        ResponseInputStream<GetObjectResponse> responseInputStreamMock =
                new ResponseInputStream<>(objectResponseMock, byteArrayInputStream);
        when(s3ClientFromFactory.getObject(GetObjectRequest.builder()
                .bucket(basicBucket)
                .key(BASIC_FILE_STORAGE_PATH)
                .build()))
                .thenReturn(responseInputStreamMock);

        ByteArrayInputStream anotherInputStream = new ByteArrayInputStream(DATA_STORAGE_ANSWER.getBytes(UTF_8));
        ResponseInputStream<GetObjectResponse> anotherResponseInputStreamMock =
                new ResponseInputStream<>(objectResponseMock, anotherInputStream);
        when(s3ClientFromFactory.getObject(GetObjectRequest.builder()
                .bucket(DATA_BUCKET)
                .key(DATA_FILE_STORAGE_PATH)
                .build()))
                .thenReturn(anotherResponseInputStreamMock);
    }

    @Test
    void testProperRouting() {
        assertThat(
                storageService.read(BasePrivateResource.forAbsolutePrivate(BASIC_STORAGE_PATH))
        ).hasContent(BASIC_STORAGE_ANSWER);

        assertThat(
                storageService.read(BasePrivateResource.forAbsolutePrivate(DATA_STORAGE_PATH))
        ).hasContent(DATA_STORAGE_ANSWER);
    }
}