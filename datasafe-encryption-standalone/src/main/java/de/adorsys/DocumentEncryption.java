package de.adorsys;

import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;

@Slf4j
//@Component
public class DocumentEncryption {
    private final EncryptedDocumentReadService reader;
    private final EncryptedDocumentWriteService writer;
    private final Properties properties;
    private Uri dir;
    private boolean PathEncryptionEnabled;
    private int keyType;

    public DocumentEncryption(Properties properties, EncryptedDocumentWriteService writer, EncryptedDocumentReadService reader) {
        this.writer = writer;
        this.reader = reader;
        this.properties = properties;
    }


    @SneakyThrows
    public void encryptWithPubKey(List<PublicKeyIDWithPublicKey> publicKeys, PrivateKey privateKey, byte[] input, String filename) {
        Path path = Paths.get(properties.getSystemRoot() + "/Encrypted/" + filename + "_encrypted");
        URI location = new URI(path.toString());
        PrivateResource privateResource = new BasePrivateResource(new Uri(location));
        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);

        Map<PublicKeyIDWithPublicKey, AbsoluteLocation> map = Map.of(
                publicKeys.get(0),
                absoluteLocation
        );
        try (OutputStream os = writer.write(map, new KeyPair(publicKeys.get(0).getPublicKey(), privateKey))) {
            os.write(input);
        }

    }


    @SneakyThrows
    public void encryptWithSecretKey(SecretKeyIDWithKey secretKey, byte[] input, String filename) {
        Path path = Paths.get(properties.getSystemRoot() + "/Encrypted/" + filename + "_encrypted");
        URI location = new URI(path.toString());
        PrivateResource privateResource = new BasePrivateResource(new Uri(location));
        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);

        try (OutputStream os = writer.write(WithCallback.noCallback(absoluteLocation), secretKey)) {
            os.write(input);
        }
    }

    @SneakyThrows
    public void decrypt(String filename, UserIDAuth user) {
        Uri location = new Uri(properties.getSystemRoot() + "/Encrypted/" + filename + "_encrypted");
        PrivateResource privateResource = new BasePrivateResource(location);
        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);

        ReadRequest<UserIDAuth, AbsoluteLocation<PrivateResource>> readRequest = ReadRequest.<UserIDAuth, AbsoluteLocation<PrivateResource>>builder()
                .location(absoluteLocation)
                .owner(user)
                .storageIdentifier(new StorageIdentifier(StorageIdentifier.DEFAULT_ID))
                .build();

        try (InputStream is = reader.read(readRequest)) {
            writeToFile(is, filename);
        }
    }
    @SneakyThrows
    private void writeToFile(InputStream is, String filename){
        dir = new Uri(properties.getSystemRoot());
        String uriPath = filename + "_decrypted" + ".txt";
        Path outputPath = Paths.get(dir.resolve(uriPath).asURI());
        if (!Files.exists(outputPath)) {
            Files.createFile(outputPath);
        }
        Files.copy(is, outputPath, StandardCopyOption.REPLACE_EXISTING);
    }

}






