package de.adorsys;

import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;

import javax.crypto.SecretKey;
import java.io.OutputStream;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class EncryptionApp {
    Properties properties;
    String readKeyPassword;
    String readStorePassword;
    DocumentEncryption documentEncryptor = new DocumentEncryption();
    Keystore keystore = new Keystore();
    Scanner scanner = new Scanner(System.in);

    public EncryptionApp() {
    }

    public void start() throws KeyStoreException {
        boolean running = true;
        while (running) {
            System.out.println("Encryption Application");
            System.out.println("Enter storage path for encrypted files");
            properties.setSystemRoot(scanner.nextLine());
            System.out.print("Enter Keystore password: ");
            readStorePassword = scanner.nextLine();
            System.out.print("Enter Key password: ");
            readKeyPassword = scanner.nextLine();

            System.out.println("Creating keystore and keys....");
            keystore.createKeyStore(readStorePassword, readKeyPassword);

            System.out.println("Choose an option:");
            System.out.println("1. Get public key");
            System.out.println("2. Get private key");
            System.out.println("3. Encrypt Document");
            System.out.println("4. Decrypt Document");
            System.out.println("5. Change Encryption Algorithm");
            System.out.println("6. Exit");

            int choice = scanner.nextInt();
            switchOption(choice, running);
        }
    }

    private void switchOption(int choice, boolean running) throws KeyStoreException {
        switch (choice) {
            case 1:
                List<PublicKeyIDWithPublicKey> publicKeys = keystore.getPublicKey();
                System.out.println("Public Keys: " + publicKeys);
                break;
            case 2:
                PrivateKey privateKey = keystore.getPrivateKey();
                System.out.println("Private key: " + privateKey);
                break;
            case 3:
                System.out.println("Enable Path Encryption? Yes/No");
                if (Objects.equals(scanner.nextLine(), "Yes")) {
                    properties.setPathEncryptionEnabled(true);
                } else {
                    properties.setPathEncryptionEnabled(false);
                }
                SecretKey secretKey = keystore.getSecretKey();
                OutputStream stream = documentEncryptor.write(readKeyPassword, secretKey);
                break;
            case 4:
                // Decrypt document
                break;
            case 5:
                System.out.println("Encryption library uses two encryption Algorythms. Namely RSA and Elliptic curve");
                System.out.println("Enter 1 for RSA or 2 for Elliptic curve encryption");
                int encryptionAlgorithm = scanner.nextInt();
            case 6:
                running = false;
                break;
        }

    }
}



