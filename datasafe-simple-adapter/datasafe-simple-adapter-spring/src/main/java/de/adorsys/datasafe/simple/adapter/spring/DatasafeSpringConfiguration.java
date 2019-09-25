package de.adorsys.datasafe.simple.adapter.spring;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Configuration
@ComponentScan(basePackageClasses = DatasafeSpringBeans.class)
@Slf4j
public class DatasafeSpringConfiguration {

    public DatasafeSpringConfiguration() {
        Security.addProvider(new BouncyCastleProvider());
    }
}
