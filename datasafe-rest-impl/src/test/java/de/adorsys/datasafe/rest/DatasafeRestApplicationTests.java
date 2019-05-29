package de.adorsys.datasafe.rest;

import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBootApplication
@EnableWebMvc
public class DatasafeRestApplicationTests extends SpringBootServletInitializer {
    public static void main(String[] args) {
        log.info("DatasafeRestApplication Test ********************");
        ApplicationContext ctx = SpringApplication.run(DatasafeRestApplicationTests.class, args);

    }
}
