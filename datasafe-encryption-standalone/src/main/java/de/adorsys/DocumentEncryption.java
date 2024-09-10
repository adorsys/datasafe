package de.adorsys;

import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;


@Component
public class DocumentEncryption {

    private EncryptedDocumentReadService reader;
    private EncryptedDocumentWriteService writer;
    private Properties properties;


//    CMSDocumentWriteService write = new CMSDocumentWriteService();

    public DocumentEncryption(Properties properties, EncryptedDocumentWriteService writer, EncryptedDocumentReadService reader) {
        this.properties = properties;
        this.writer = writer;
        this.reader = reader;
    }

    @SneakyThrows
    public void write(SecretKeyIDWithKey secretKey, String filename) {
        Uri location = new Uri(properties.getSystemRoot());
        PrivateResource privateResource = new BasePrivateResource(location);
        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);

        try (OutputStream outputStream = writer.write(WithCallback.noCallback(absoluteLocation), secretKey)) {
            outputStream.write(Files.readAllBytes(Paths.get(properties.getSystemRoot() + "/" + filename)));
        }
    }

//    public InputStream read( String password, SecretKey secretKey) {
//        SecretKeyIDWithKey secretKeyIDWithKey = new SecretKeyIDWithKey(new KeyID(password), secretKey);
//        Uri location = new Uri(properties.getSystemRoot() + "/EncryptedDocuments");
//        PrivateResource privateResource = new BasePrivateResource(location);
//        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);
//
//        ReadRequest readRequest
//
//        return reader.read(WithCallback.<AbsoluteLocation<PrivateResource>, ResourceWriteCallback>builder()
//               .wrapped(absoluteLocation).callbacks(null).build(), secretKeyIDWithKey);
//    }

}
