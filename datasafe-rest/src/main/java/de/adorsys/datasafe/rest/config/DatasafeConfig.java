package de.adorsys.datasafe.rest.config;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.storage.FileSystemStorageService;
import de.adorsys.datasafe.business.impl.storage.S3StorageService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.nio.file.Paths;
import java.security.Security;

@Configuration
public class DatasafeConfig {

    @Bean
    StorageService storageService() {
        return new FileSystemStorageService(Paths.get("/Users/valentyn.berezin/IdeaProjects/datasafe/datasafe-rest/target/storagezzz").toUri());
    }

    @Bean
    DefaultDatasafeServices datasafeService(DatasafeProperties properties, StorageService storageService) {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerDefaultDatasafeServices
                .builder()
                .config(new DFSConfig() {
                    @Override
                    public String keystorePassword() {
                        return properties.getKeystorePassword();
                    }

                    @Override
                    public URI systemRoot() {
                        return URI.create(properties.getSystemRoot());
                    }
                })
                .storageList(storageService)
                .storageRead(storageService)
                .storageWrite(storageService)
                .storageRemove(storageService)
                .storageCheck(storageService)
                .build();
    }
}
