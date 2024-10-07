package de.adorsys;

import de.adorsys.config.Config;
import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Interface {
    private final Properties properties = new Properties();
    private EncryptionServices.EncryptionServicesImpl encryptionServices;
    private DocumentEncryption documentEncryption;
    private KeyStoreOper keyStoreOper;
    private Userprofile userprofile;
    private Scanner scanner;
    private boolean running;
    private String readKeyPassword;
    private String readStorePassword;
    private UserIDAuth user;
    private Path storagePath;
    private int keyType;


    public Interface() {
    }

    @SneakyThrows
    public void start() {
        running = true;
        scanner = new Scanner(System.in);

        System.out.println("Encryption Application");

        if (properties.getSystemRoot() == null) {
            System.out.println("Enter storage path where you have stored txt files to be encrypted");
            storagePath = Paths.get(scanner.nextLine());
        } else {
            storagePath = Paths.get(properties.getSystemRoot());
        }

        System.out.println("Enter a profile name");
        String name = scanner.nextLine();
        System.out.print("Enter a password for the keystore: ");
        readStorePassword = scanner.nextLine();
        System.out.print("Enter a password to read keys from the Keystore ");
        readKeyPassword = scanner.nextLine();

        System.out.println("Would you like to Encrypt and decrypt document with a Public Key / Private Key or a secret key ");
        System.out.println("Enter 1 for Pub/Priv key or 2 for secret key");
        keyType = Integer.parseInt(scanner.nextLine());

        System.out.println("The encryption library uses two encryption Algorithms. Namely RSA and Elliptic curve");
        System.out.println("Enter 1 for RSA or 2 for Elliptic curve encryption");
        int algo = Integer.parseInt(scanner.nextLine());


        encryptionServices = Config.encryptionServices(storagePath, new ReadStorePassword(readStorePassword), algo);

        userprofile = encryptionServices.userprofile();
        user = new UserIDAuth(name, new ReadKeyPassword(readKeyPassword.toCharArray()));
        userprofile.createProfile(user);

        documentEncryption = encryptionServices.documentEncryption(properties, keyType);
        keyStoreOper = encryptionServices.keyStoreOper(properties);
        keyStoreOper.createKeyStore(userprofile.getUserProfile(user), user);

        while (running) {
            System.out.println("Choose an option:");
            System.out.println("1. Encrypt Document");
            System.out.println("2. Decrypt Document");
            System.out.println("3. Change Encryption Algorithm");
            System.out.println("4. Create new Profile");
            System.out.println("5. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();
            switchOption(choice);
        }
    }

    @SneakyThrows
    private void switchOption(int choice) {
        switch (choice) {
            case 1:
                System.out.println("Enable Path Encryption? Yes / No");
                String isEncryptionEnabled = scanner.nextLine();
                documentEncryption.enablePathEncryption(isEncryptionEnabled);

                System.out.println("Please enter file name to be encrypted");
                String filename = scanner.nextLine();
                documentEncryption.write(keyStoreOper.getPublicKey(), keyStoreOper.getPrivateKey(user), keyStoreOper.getSecretKey(), filename);
                break;
            case 2:
                System.out.println("Please enter file name to be decrypted");
                String encryptedFilename = scanner.nextLine();
                documentEncryption.read(encryptedFilename, user);
                break;
            case 3:
                System.out.println("Enter 1 for RSA or 2 for EC (Elliptic Curve Encryption)");
                int algo = Integer.parseInt(scanner.nextLine());

                encryptionServices = Config.encryptionServices(storagePath, new ReadStorePassword(readStorePassword), algo);

                documentEncryption = encryptionServices.documentEncryption(properties, keyType);
                keyStoreOper = encryptionServices.keyStoreOper(properties);
                keyStoreOper.createKeyStore(userprofile.getUserProfile(user), user);

            case 4:
                System.out.println("Enter a new profile name");
                String newName = scanner.nextLine();
                System.out.print("Enter a new password for the keystore: ");
                readStorePassword = scanner.nextLine();
                System.out.print("Enter a new password to read keys from the Keystore ");
                readKeyPassword = scanner.nextLine();

                user = new UserIDAuth(newName, new ReadKeyPassword(readKeyPassword.toCharArray()));
                userprofile.createProfile(user);
                keyStoreOper.createKeyStore(userprofile.getUserProfile(user), user);
                break;
            case 5:
                running = false;
                break;
        }

    }
}



