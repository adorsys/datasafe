package de.adorsys;

import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
public class EncryptionApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(EncryptionApplication.class, args);
    }

    @SneakyThrows
    @Override
    public void run(String... args) {
        Interface application = new Interface();
        application.start();
    }
}
