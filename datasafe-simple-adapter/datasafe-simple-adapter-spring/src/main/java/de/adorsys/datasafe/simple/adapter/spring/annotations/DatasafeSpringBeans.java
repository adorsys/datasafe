package de.adorsys.datasafe.simple.adapter.spring.annotations;

import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
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
    public SimpleDatasafeService simpleDatasafeService() {
        return new SimpleDatasafeServiceImpl();

    }
}
