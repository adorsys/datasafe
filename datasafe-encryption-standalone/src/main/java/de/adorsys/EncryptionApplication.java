package de.adorsys;

import java.io.IOException;
import java.security.KeyStoreException;

public class EncryptionApplication {
    public static void main(String[] args) throws KeyStoreException, IOException {
        Interface application = new Interface();
        application.start();
    }

}
