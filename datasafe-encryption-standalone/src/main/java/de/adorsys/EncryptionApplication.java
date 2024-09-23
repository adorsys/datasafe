package de.adorsys;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.security.KeyStoreException;

@SpringBootApplication()
public class EncryptionApplication {
    public static void main(String[] args) throws KeyStoreException {
        Interface application = new Interface();
        application.start();
    }

}
