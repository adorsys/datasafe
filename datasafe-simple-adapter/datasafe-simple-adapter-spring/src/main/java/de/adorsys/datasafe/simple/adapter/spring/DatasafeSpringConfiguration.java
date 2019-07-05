package de.adorsys.datasafe.simple.adapter.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = DatasafeSpringBeans.class)
@Slf4j
public class DatasafeSpringConfiguration {
}
