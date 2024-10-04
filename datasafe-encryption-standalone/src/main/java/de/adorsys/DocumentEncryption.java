package de.adorsys;

import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
//@Component
public class DocumentEncryption {
    private final EncryptedDocumentReadService reader;
    private final EncryptedDocumentWriteService writer;
    private boolean PathEncryptionEnabled;
    private Properties properties;

    public DocumentEncryption(Properties properties, EncryptedDocumentWriteService writer, EncryptedDocumentReadService reader) {
        this.writer = writer;
        this.reader = reader;
        this.properties = properties;
    }
    public void enablePathEncryption(String isPathEncryptEnabled ) {
        if (Objects.equals(isPathEncryptEnabled, "Yes")) {
            PathEncryptionEnabled = true;
        } else {
            PathEncryptionEnabled = false;
        }
    }

    @SneakyThrows
    public void write(SecretKeyIDWithKey secretKey, String filename) {
        Uri location = new Uri(properties.getSystemRoot() + "/Encrypted/" + "Output");
        PrivateResource privateResource = new BasePrivateResource(location);
        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);

        try (OutputStream outputStream = writer.write(WithCallback.noCallback(absoluteLocation), secretKey)) {
            outputStream.write("Hello".getBytes(UTF_8));
        }
    }
    @SneakyThrows
    public void read(String filename, UserIDAuth user) {
        Uri location = new Uri(properties.getSystemRoot()+ "/Encrypted/" + "Output");
        PrivateResource privateResource = new BasePrivateResource(location);
        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);

        ReadRequest<UserIDAuth, AbsoluteLocation<PrivateResource>> readRequest = ReadRequest.<UserIDAuth, AbsoluteLocation<PrivateResource>>builder()
                .location(absoluteLocation)
                .owner(user)
                .storageIdentifier(new StorageIdentifier(StorageIdentifier.DEFAULT_ID))
                .build();

        try( InputStream inputStream = reader.read(readRequest)) {
            System.out.println(StreamUtils.copyToString(inputStream, UTF_8));
        }
    }

}
