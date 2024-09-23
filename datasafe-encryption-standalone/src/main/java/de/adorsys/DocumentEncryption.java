package de.adorsys;

import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;


@Component
public class DocumentEncryption {
    private final EncryptedDocumentReadService reader;
    private final EncryptedDocumentWriteService writer;
    private Properties properties;

    public DocumentEncryption(EncryptedDocumentWriteService writer, EncryptedDocumentReadService reader) {
//        this.properties = properties;
        this.writer = writer;
        this.reader = reader;
    }

    @SneakyThrows
    public void write(SecretKeyIDWithKey secretKey, String filename) {
        Uri location = new Uri(properties.getSystemRoot() + "/" + filename);
        PrivateResource privateResource = new BasePrivateResource(location);
        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);

        try (OutputStream outputStream = writer.write(WithCallback.noCallback(absoluteLocation), secretKey)) {
            outputStream.write(Files.readAllBytes(Paths.get(properties.getSystemRoot()+ "/" + filename)));
        }
    }

//    public int read( String password, SecretKeyIDWithKey secretKey, String filename ) {
//        Uri location = new Uri(properties.getSystemRoot() + "/" + filename);
//        PrivateResource privateResource = new BasePrivateResource(location);
//        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);
//        ReadRequest readRequest = ReadRequest.<UserIDAuth, AbsoluteLocation<PrivateResource>>builder()
//                .location(resolver.resolveRelativeToPrivateInbox(request.getOwner(), request.getLocation()))
//                .owner(request.getOwner())
//                .storageIdentifier(request.getStorageIdentifier())
//                .build();
//
//        try(InputStream inputStream = reader.read(WithCallback.noCallback(absoluteLocation), secretKey)) {
//            return inputStream.read(Files.readAllBytes(Paths.get(properties.getSystemRoot()+ "/" + filename)));
//
//        }
//    }

}
