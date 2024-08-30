package de.adorsys;

import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;

import javax.crypto.SecretKey;
import java.security.KeyStoreException;
import java.util.Objects;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws KeyStoreException {
        EncryptionApp encryptionApp = new EncryptionApp();
        encryptionApp.start();
    }
}
