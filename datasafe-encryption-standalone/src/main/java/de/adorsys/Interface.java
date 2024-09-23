package de.adorsys;

import de.adorsys.config.Config;
import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentReadService;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentWriteService;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Interface {
    private final Properties properties = new Properties();
    private EncryptionServices.EncryptionServicesImpl encryptionServices;
    private DocumentEncryption documentEncryption;
    private KeyStoreOper keyStoreOper;
    private Config config;
    Scanner scanner = new Scanner(System.in);
    private boolean running;
    private String readKeyPassword;
    private String readStorePassword;


    public Interface() {
    }

    public void start() throws KeyStoreException {
        running = true;
        System.out.println("Encryption Application");
        System.out.println("Enter storage path where you have stored txt files to be encrypted");
        Path storagePath = Paths.get(scanner.nextLine());
        System.out.print("Enter a password for the KeyStoreOper: ");
        readStorePassword = scanner.nextLine();
        System.out.print("Enter a password to read keys from the KeyStoreOper ");
        readKeyPassword = scanner.nextLine();

        encryptionServices = Config.encryptionServices(storagePath,new ReadStorePassword(readStorePassword));
        documentEncryption = encryptionServices.documentEncryption();
        keyStoreOper = encryptionServices.keyStoreOper();

        while (running) {
            System.out.println("Creating keyStoreOper and keys....");
            keyStoreOper.createKeyStore(readStorePassword, readKeyPassword);

            System.out.println("Choose an option:");
            System.out.println("1. Get public key");
            System.out.println("2. Get private key");
            System.out.println("3. Encrypt Document");
            System.out.println("4. Decrypt Document");
            System.out.println("5. Change Encryption Algorithm");
            System.out.println("6. Exit");

            int choice = scanner.nextInt();
            switchOption(choice);
        }
    }

    private void switchOption(int choice) throws KeyStoreException {
        switch (choice) {
            case 1:
                List<PublicKeyIDWithPublicKey> publicKeys = keyStoreOper.getPublicKey();
                System.out.println("Public Keys: " + publicKeys);
                break;
            case 2:
                PrivateKey privateKey = keyStoreOper.getPrivateKey();
                System.out.println("Private key: " + privateKey);
                break;
            case 3:
//                System.out.println("Enable Path Encryption? Yes/No");
//                String encryptionEnabled = scanner.nextLine();
//                if (Objects.equals(encryptionEnabled, "Yes")) {
//                    properties.setPathEncryptionEnabled(true);
//                } else {
//                    properties.setPathEncryptionEnabled(false);
//                }
                System.out.println("Please enter file name to be encrypted");
                String filename = scanner.nextLine();
                documentEncryption.write(keyStoreOper.getSecretKey(), filename);
                break;
            case 4:
                // Decrypt document
                break;
            case 5:
                System.out.println("The encryption library uses two encryption Algorythms. Namely RSA and Elliptic curve");
                System.out.println("Enter 1 for RSA or 2 for Elliptic curve encryption");
                int encryptionAlgorithm = scanner.nextInt();
            case 6:
                running = false;
                break;
        }

    }
}



