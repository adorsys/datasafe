package de.adorsys.config;

import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
@Slf4j
@Configuration
public class StorageConfig {
    public static final String FILESYSTEM_ENV = "USE_FILESYSTEM";

    @Bean
    @ConditionalOnProperty(FILESYSTEM_ENV)
    StorageService singleStorageServiceFilesystem(Properties properties) {
        String root = System.getenv(FILESYSTEM_ENV);
        log.info("==================== FILESYSTEM");
        log.info("build to FILESYSTEM with root " + root);
        properties.setSystemRoot(root);
        return new FileSystemStorageService(Paths.get(root));
    }
}
