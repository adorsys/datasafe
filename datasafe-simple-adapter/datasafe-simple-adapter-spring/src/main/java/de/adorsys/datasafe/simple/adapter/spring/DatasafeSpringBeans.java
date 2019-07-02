package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.impl.SimpleDatasafeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DatasafeSpringBeans {
    public DatasafeSpringBeans() {
        log.info("INIT");
    }

    @Bean
    SpringSimpleDatasafeServiceFactory simpleDatasafeServiceFactory(SpringDFSCredentialProperties springDFSCredentialProperties) {
        if (springDFSCredentialProperties != null) {
            log.info("PETER IS HAPPY: " + springDFSCredentialProperties);
        }
        else {
            throw new RuntimeException("spring properties are not set");
        }

        return new SpringSimpleDatasafeServiceFactory(getDFSCredentialsFromSpringDFSCredentialProperties(springDFSCredentialProperties));
    }

    private DFSCredentials getDFSCredentialsFromSpringDFSCredentialProperties(SpringDFSCredentialProperties springDFSCredentialProperties) {
        return null;
    }

    @Bean
    public SimpleDatasafeService simpleDatasafeService() {

        return new SimpleDatasafeServiceImpl();

    }
}
