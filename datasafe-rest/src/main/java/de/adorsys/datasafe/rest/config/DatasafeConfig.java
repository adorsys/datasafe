package de.adorsys.datasafe.rest.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.storage.S3StorageService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.security.Security;

@Configuration
public class DatasafeConfig {

    @Value("${AWS_BUCKET}")
    private String bucketName;

    @Value("${datasafe.system.root}")
    private String systemRoot;

    @Value("${datasafe.keystore.password}")
    private String keystorePassword;

    private final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .enablePathStyleAccess()
            .build();

    @Bean
    StorageService storageService() {
        return new S3StorageService(s3, bucketName);
    }

    @Bean
    DefaultDatasafeServices datasafeService() {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerDefaultDatasafeServices
                .builder()
                .config(new DFSConfig() {
                    @Override
                    public String keystorePassword() {
                        return keystorePassword;
                    }

                    @Override
                    public URI systemRoot() {
                        return URI.create(systemRoot);
                    }
                })
                .storageList(new S3StorageService(s3, bucketName))
                .storageRead(new S3StorageService(s3, bucketName))
                .storageWrite(new S3StorageService(s3, bucketName))
                .storageRemove(new S3StorageService(s3, bucketName))
                .storageCheck(new S3StorageService(s3, bucketName))
                .build();
    }
}
