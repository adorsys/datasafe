package de.adorsys;

import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.CmsEncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentReadService;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentWriteService;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.*;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


@Component
public class DocumentEncryption {

    private EncryptedDocumentWriteService writer;

    private EncryptedDocumentReadService reader;
    private Properties properties;
    public DocumentEncryption() {
    }

    public OutputStream write( String password, SecretKey secretKey) {

        SecretKeyIDWithKey secretKeyIDWithKey = new SecretKeyIDWithKey (new KeyID(password), secretKey);
        Uri location = new Uri(properties.getSystemRoot() + "/EncryptedDocuments");
        PrivateResource privateResource = new BasePrivateResource(location);
        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);

        List<ResourceWriteCallback> callback = null;

        return writer.write(WithCallback.<AbsoluteLocation<PrivateResource>, ResourceWriteCallback>builder()
                .wrapped(absoluteLocation).callbacks(callback).build(), secretKeyIDWithKey);
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
