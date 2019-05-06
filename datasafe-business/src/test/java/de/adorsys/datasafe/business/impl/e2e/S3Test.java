package de.adorsys.datasafe.business.impl.e2e;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import de.adorsys.datasafe.business.impl.storage.S3StorageService;
import de.adorsys.datasafe.business.impl.testcontainers.DaggerTestDocusafeServices;
import org.junit.jupiter.api.BeforeEach;

public class S3Test extends StorageTest {

    private String accessKeyID = System.getProperty("AWS_ACCESS_KEY");
    private String secretAccessKey = System.getProperty("AWS_SECRET_KEY");
    private String region = "eu-central-1";
    private String bucketName = "adorsys-docusafe";
    private BasicAWSCredentials creds = new BasicAWSCredentials(accessKeyID, secretAccessKey);
    private AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(creds))
            .withRegion(region)
            .enablePathStyleAccess()
            .build();

    @BeforeEach
    void init() {
        this.location = location;
        this.storage = new S3StorageService(s3, bucketName);

        this.services = DaggerTestDocusafeServices.builder()
                .storageList(new S3StorageService(s3, bucketName))
                .storageRead(new S3StorageService(s3, bucketName))
                .storageWrite(new S3StorageService(s3, bucketName))
                .build();
    }
}
