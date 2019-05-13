package de.adorsys.datasafe.business.impl.e2e;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.storage.S3StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URI;

import static de.adorsys.datasafe.business.impl.e2e.S3Test.KEY;

@EnabledIfEnvironmentVariable(named = KEY, matches = ".+")
public class S3Test extends BaseStorageTest {

    static final String KEY = "AWS_ACCESS_KEY";

    private String accessKeyID = System.getProperty(KEY);
    private String secretAccessKey = System.getProperty("AWS_SECRET_KEY");
    private String region = System.getProperty("AWS_REGION", "eu-central-1");
    private String bucketName = System.getProperty("AWS_BUCKET", "adorsys-docusafe");

    private BasicAWSCredentials creds = new BasicAWSCredentials(accessKeyID, secretAccessKey);

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(creds))
            .withRegion(region)
            .enablePathStyleAccess()
            .build();

    @BeforeEach
    void init() {
        // FIXME: travis runs builds in parrallel - we can't have same users on shared resource, so adding entropy
        this.location = URI.create("s3://" +  bucketName + "/" + System.currentTimeMillis() + "/");
        this.storage = new S3StorageService(s3, bucketName);

        this.services = DaggerDefaultDatasafeServices
                .builder()
                .config(new DFSConfig() {
                    @Override
                    public String keystorePassword() {
                        return "PAZZWORD";
                    }

                    @Override
                    public URI systemRoot() {
                        return location;
                    }
                })
                .storageList(new S3StorageService(s3, bucketName))
                .storageRead(new S3StorageService(s3, bucketName))
                .storageWrite(new S3StorageService(s3, bucketName))
                .storageRemove(new S3StorageService(s3, bucketName))
                .build();

        loadReport.add("Test with Amazon S3");
    }

    @AfterEach
    void cleanUp() {
        removeUser(john);
        removeUser(jane);
    }
}
