package de.adorsys;

import com.google.common.io.MoreFiles;
import de.adorsys.config.EncryptionConfig;
import de.adorsys.config.EncryptionServices;
import de.adorsys.config.Properties;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

@Slf4j
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
    private String keyType;
    private String algorithm;
    private Uri dir;


    public Interface() {
    }

    @SneakyThrows
    public void start() {
        running = true;
        scanner = new Scanner(System.in);

        System.out.println("----------------Encryption Application-----------------");

        System.out.println("Enter storage path where you have stored txt files, to be encrypted");
        System.out.println("Format example :  /Users/YourUsername/Documents/YourDirectory/");
        String absolutePath = "file://" + scanner.nextLine();
        properties.setSystemRoot(absolutePath);
        storagePath = Paths.get(absolutePath);


        System.out.println("Enter a profile name");
        String name = scanner.nextLine();
        System.out.print("Enter a password for the keystore and to read keys: ");
        readStorePassword = scanner.nextLine();
        readKeyPassword = readStorePassword;

        System.out.println("Would you like to use Asymmetric (Pub/Priv key) or Symmetric encryption (Secret key) ? ");
        System.out.println("Enter 'PUB' for Asymmetric or 'SEC' for Symmetric encryption");
        keyType = scanner.nextLine();

        System.out.println("The encryption library uses two encryption Algorithms. Namely RSA and Elliptic curve");
        System.out.println("Elliptic curve will be used to by default for the key creation");
        algorithm = "EC";

        encryptionServices = EncryptionConfig.encryptionServices(storagePath, new ReadStorePassword(readStorePassword), algorithm);


        userprofile = encryptionServices.getUserprofile();
        documentEncryption = encryptionServices.getDocumentEncryption(properties);
        keyStoreOper = encryptionServices.getKeyStoreOper();

        user = new UserIDAuth(name, new ReadKeyPassword(readKeyPassword.toCharArray()));
        userprofile.createPrivProfile(user);
        keyStoreOper.createKeyStore(userprofile.getUserPrivProfile(user), user);

        System.out.println("Press Enter to continue...");
        scanner.nextLine();

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
    private byte[] InputfiletoBytes(String filename) {
        dir = new Uri(properties.getSystemRoot());
        Path inputFilePath = Paths.get(dir.resolve(filename).asURI());

        return MoreFiles.asByteSource(inputFilePath, StandardOpenOption.READ).read();
    }

    @SneakyThrows
    private void switchOption(int choice) {
        switch (choice) {
            case 1:
                System.out.println("Please enter file name to be encrypted, that is stored in the directory (eg. yourfile.txt) : " + properties.getSystemRoot());
                String filename = scanner.nextLine();

                UserPrivateProfile userPrivateProfile = userprofile.getUserPrivProfile(user);

                if (keyType.equalsIgnoreCase("PUB")) {

                    documentEncryption.encryptWithPubKey(
                            keyStoreOper.getPublicKey(user, userPrivateProfile),
                            keyStoreOper.getPrivateKey(user, userPrivateProfile),
                            InputfiletoBytes(filename), filename
                    );
                } else if (keyType.equalsIgnoreCase("SEC")) {

                    documentEncryption.encryptWithSecretKey(
                            keyStoreOper.getSecretKey(user, userPrivateProfile),
                            InputfiletoBytes(filename), filename
                    );
                }
                break;
            case 2:
                System.out.println("Please enter file name to be decrypted (eg. yourfile.txt)");
                String encryptedFilename = scanner.nextLine();
                documentEncryption.decrypt(encryptedFilename, user);
                System.out.println("File decrypted and stored in: " + properties.getSystemRoot());
                break;
            case 3:
                System.out.println("Enter 'EC' (Elliptic Curve Encryption) or 'RSA' to switch encryption algorithm");
                algorithm = scanner.nextLine();

                encryptionServices = EncryptionConfig.encryptionServices(storagePath, new ReadStorePassword(readStorePassword), algorithm);

                documentEncryption = encryptionServices.getDocumentEncryption(properties);
                keyStoreOper = encryptionServices.getKeyStoreOper();
                keyStoreOper.createKeyStore(userprofile.getUserPrivProfile(user), user);
                break;

            case 4:
                System.out.println("Enter a new profile name");
                String name = scanner.nextLine();
                System.out.print("Enter a password for the keystore and to read keys: ");
                readStorePassword = scanner.nextLine();
                readKeyPassword = readStorePassword;

                System.out.println("Would you like to use Asymmetric (Pub/Priv key) or Symmetric encryption (Secret key) ? ");
                System.out.println("Enter 'PUB' for Asymmetric or 'SEC' for Symmetric encryption");
                keyType = scanner.nextLine();

                System.out.println("Enter 'EC' (Elliptic Curve Encryption) or 'RSA' to switch encryption algorithm  (Case sensitive)");
                algorithm = scanner.nextLine();

                encryptionServices = EncryptionConfig.encryptionServices(storagePath, new ReadStorePassword(readStorePassword), algorithm);
                documentEncryption = encryptionServices.getDocumentEncryption(properties);
                keyStoreOper = encryptionServices.getKeyStoreOper();

                user = new UserIDAuth(name, new ReadKeyPassword(readKeyPassword.toCharArray()));
                userprofile.createPrivProfile(user);
                keyStoreOper.createKeyStore(userprofile.getUserPrivProfile(user), user);
                break;
            case 5:
                running = false;
                break;
        }

    }
}



