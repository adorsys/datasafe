package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.simple.adapter.spring.annotations.DatasafeSpringBeans;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {DatasafeSpringBeans.class})
@Slf4j
public class DatasafeSpringConfiguration {

    public DatasafeSpringConfiguration() {
        log.info("CREATING DATASAFE BEANS");
    }
}
